/*
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Centrys Inc. <https://centrys.io>
 */

package org.aion;

import org.aion.api.server.http.RpcServer;
import org.aion.api.server.http.RpcServerBuilder;
import org.aion.api.server.http.RpcServerVendor;
import org.aion.api.server.http.nano.NanoRpcServer;
import org.aion.api.server.http.undertow.UndertowRpcServer;
import org.aion.api.server.pb.ApiAion0;
import org.aion.api.server.pb.IHdlr;
import org.aion.api.server.zmq.HdlrZmq;
import org.aion.api.server.zmq.ProtocolProcessor;
import org.aion.engine.impl.Aion0BlockchainEngine;
import org.aion.generic.IBlockchainEngine;
import org.aion.mcf.config.CfgApiRpc;
import org.aion.mcf.config.CfgSsl;
import org.aion.mcf.mine.IMineRunner;
import org.aion.zero.impl.blockchain.AionImpl;
import org.aion.zero.impl.blockchain.IChainInstancePOW;
import org.aion.zero.impl.config.CfgAion;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class BlockchainEngineFactory {

    public static IBlockchainEngine create(CfgAion cfg, Logger genLog, char[] sslPass) {
        switch (cfg.getConsensus().getConsensusType()) {
            case POW:
                IChainInstancePOW ac = AionImpl.inst();
                ProtocolProcessor protocolProcessor = getAion0ProtocolProcessor(ac, cfg);
                Thread zmqThread = startAion0Api(protocolProcessor, cfg);
                IMineRunner miner = startAion0Mining(ac, cfg);
                RpcServer rpcServer = startAion0RpcServer(ac, cfg, genLog, sslPass);
                startShutdownThread(ac, genLog, zmqThread, miner, protocolProcessor, rpcServer);

                return new Aion0BlockchainEngine(ac);
            default:
                return null;
        }
    }

    private static ProtocolProcessor getAion0ProtocolProcessor(IChainInstancePOW ac, CfgAion cfg) {
        ProtocolProcessor processor = null;
        if (cfg.getApi().getZmq().getActive()) {
            IHdlr handler = new HdlrZmq(new ApiAion0(ac));
            processor = new ProtocolProcessor(handler, cfg.getApi().getZmq());
        }
        return processor;
    }

    private static Thread startAion0Api(ProtocolProcessor protocolProcessor, CfgAion cfg) {
        Thread zmqThread = null;
        if (cfg.getApi().getZmq().getActive()) {
            zmqThread = new Thread(protocolProcessor, "zmq-api");
            zmqThread.start();
        }
        return zmqThread;
    }

    private static IMineRunner startAion0Mining(IChainInstancePOW ac, CfgAion cfg) {
        IMineRunner nm = null;

        if (!cfg.getConsensus().isSeed()) {
            nm = ac.getBlockMiner();
        }

        if (nm != null) {
            nm.delayedStartMining(10);
        }

        return nm;
    }

    private static RpcServer startAion0RpcServer(IChainInstancePOW ac, CfgAion cfg, Logger genLog, char[] sslPass) {
        RpcServer rpcServer = null;
        if(cfg.getApi().getRpc().isActive()) {
            CfgApiRpc rpcCfg =  cfg.getApi().getRpc();

            Consumer<RpcServerBuilder<? extends RpcServerBuilder<?>>> commonRpcConfig = (rpcBuilder) -> {
                rpcBuilder.setUrl(rpcCfg.getIp(), rpcCfg.getPort());
                rpcBuilder.enableEndpoints(rpcCfg.getEnabled());

                rpcBuilder.setWorkerPoolSize(rpcCfg.getWorkerThreads());
                rpcBuilder.setIoPoolSize(rpcCfg.getIoThreads());
                rpcBuilder.setRequestQueueSize(rpcCfg.getRequestQueueSize());
                rpcBuilder.setStuckThreadDetectorEnabled(rpcCfg.isStuckThreadDetectorEnabled());

                if (rpcCfg.isCorsEnabled())
                    rpcBuilder.enableCorsWithOrigin(rpcCfg.getCorsOrigin());

                CfgSsl cfgSsl = rpcCfg.getSsl();
                if (cfgSsl.getEnabled())
                    rpcBuilder.enableSsl(cfgSsl.getCert(), sslPass);
            };
            RpcServerVendor rpcVendor = RpcServerVendor.fromString(rpcCfg.getVendor()).orElse(RpcServerVendor.UNDERTOW);
            try {
                switch (rpcVendor) {
                    case NANO: {
                        NanoRpcServer.Builder rpcBuilder = new NanoRpcServer.Builder().withAionChain(ac);
                        commonRpcConfig.accept(rpcBuilder);
                        rpcServer = rpcBuilder.build();
                        break;
                    }
                    case UNDERTOW:
                    default: {
                        UndertowRpcServer.Builder rpcBuilder = new UndertowRpcServer.Builder().withAionChain(ac);
                        commonRpcConfig.accept(rpcBuilder);
                        rpcServer = rpcBuilder.build();
                        break;
                    }
                }
            } catch (Exception e) {
                genLog.error("Failed to instantiate RPC server.", e);
            }

            if (rpcServer == null)
                throw new IllegalStateException("Issue with RPC settings caused server instantiation to fail. " +
                        "Please check RPC settings in config file.");

            rpcServer.start();
        }
        return rpcServer;
    }

    /**
     *  TODO: I would see this passed as a closure/Interface to `IBlockchainEngine` and structure (shutdown) hooks in a dedicated package.
     */
    private static void startShutdownThread(IChainInstancePOW ac, Logger genLog, Thread zmqThread, IMineRunner miner,
                                            ProtocolProcessor protocolProcessor, RpcServer rpcServer) {
        class ShutdownThreadHolder {

            private final Thread zmqThread;
            private final IMineRunner miner;
            private final ProtocolProcessor pp;
            private final RpcServer rpc;

            private ShutdownThreadHolder(Thread zmqThread, IMineRunner nm, ProtocolProcessor pp, RpcServer rpc) {
                this.zmqThread = zmqThread;
                this.miner = nm;
                this.pp = pp;
                this.rpc = rpc;
            }
        }

        ShutdownThreadHolder holder = new ShutdownThreadHolder(zmqThread, miner, protocolProcessor, rpcServer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            genLog.info("Starting shutdown process...");

            if (holder.rpc != null) {
                genLog.info("Shutting down RpcServer");
                holder.rpc.stop();
                genLog.info("Shutdown RpcServer ... Done!");
            }

            if (holder.pp != null) {
                genLog.info("Shutting down zmq ProtocolProcessor");
                try {
                    holder.pp.shutdown();
                    genLog.info("Shutdown zmq ProtocolProcessor... Done!");
                } catch (InterruptedException e) {
                    genLog.info("Shutdown zmq ProtocolProcessor failed! {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

            if (holder.zmqThread != null) {
                genLog.info("Shutting down zmq thread");
                try {
                    holder.zmqThread.interrupt();
                    genLog.info("Shutdown zmq thread... Done!");
                } catch (Exception e) {
                    genLog.info("Shutdown zmq thread failed! {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

            if (holder.miner != null) {
                genLog.info("Shutting down sealer");
                holder.miner.stopMining();
                holder.miner.shutdown();
                genLog.info("Shutdown sealer... Done!");
            }

            genLog.info("Shutting down the AionHub...");
            ac.getAionHub().close();

            genLog.info("---------------------------------------------");
            genLog.info("| Aion kernel graceful shutdown successful! |");
            genLog.info("---------------------------------------------");

        }, "shutdown"));
    }
}