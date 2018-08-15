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
 *     Aion foundation.
 */
package org.aion;


import org.aion.crypto.ECKeyFac;
import org.aion.crypto.HashUtil;
import org.aion.evtmgr.EventMgrModule;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.aion.mcf.config.CfgConsensus;
import org.aion.mcf.config.CfgSsl;
import org.aion.zero.impl.cli.Cli;
import org.aion.zero.impl.config.CfgAion;
import org.slf4j.Logger;

import java.io.Console;
import java.util.ServiceLoader;

import static org.aion.crypto.ECKeyFac.ECKeyType.ED25519;
import static org.aion.crypto.HashUtil.H256Type.BLAKE2B_256;
import static org.aion.zero.impl.Version.KERNEL_VERSION;

public class Aion {

    public static void main(String args[]) {

        /*
         * @ATTENTION: ECKey have two layer: tx layer is KeyFac optional,
         *             network layer is hardcode to secp256.
         */
        ECKeyFac.setType(ED25519);
        HashUtil.setType(BLAKE2B_256);
        ServiceLoader.load(EventMgrModule.class);

        CfgAion cfg = CfgAion.inst();
        if (args != null && args.length > 0) {
            int ret = new Cli().call(args, cfg);
            System.exit(ret);
        }

        /*
         * if in the config.xml id is set as default [NODE-ID-PLACEHOLDER]
         * return true which means should save back to xml config
         */
        if (cfg.fromXML()) {
            cfg.toXML(new String[]{"--id=" + cfg.getId()});
        }

        try {
            ServiceLoader.load(AionLoggerFactory.class);
        } catch (Exception e) {
            System.out.println("load AionLoggerFactory service fail!" + e.toString());
            throw e;
        }

        /* Outputs relevant logger configuration */
        if (!cfg.getLog().getLogFile()) {
            System.out
                .println("Logger disabled; to enable please check log settings in config.xml\n");
        } else if (!cfg.getLog().isValidPath() && cfg.getLog().getLogFile()) {
            System.out.println("File path is invalid; please check log setting in config.xml\n");
            return;
        } else if (cfg.getLog().isValidPath() && cfg.getLog().getLogFile()) {
            System.out.println("Logger file path: '" + cfg.getLog().getLogPath() + "'\n");
        }

        // get the ssl password synchronously from the console, only if required
        // do this here, before writes to logger because if we don't do this here, then
        // it gets presented to console out of order with the rest of the logging ...
        final char[] sslPass = getSslPassword(cfg);

        // from now on, all logging to console and file happens asynchronously

        /*
         * Logger initialize with LOGFILE and LOGPATH (user config inputs)
         */
        AionLoggerFactory
            .init(cfg.getLog().getModules(), cfg.getLog().getLogFile(), cfg.getLog().getLogPath());
        Logger genLog = AionLoggerFactory.getLogger(LogEnum.GEN.name());

        String logo =
              "\n                     _____                  \n" +
                "      .'.       |  .~     ~.  |..          |\n" +
                "    .'   `.     | |         | |  ``..      |\n" +
                "  .''''''''`.   | |         | |      ``..  |\n" +
                ".'           `. |  `._____.'  |          ``|\n\n";

        // always print the version string in the center of the Aion logo
        String versionStr = "v"+KERNEL_VERSION;
        int leftPad = Math.round((44 - versionStr.length()) / 2.0f) + 1;
        StringBuilder padVersionStr = new StringBuilder();
        for (int i = 0; i < leftPad; i++) padVersionStr.append(" ");
        padVersionStr.append(versionStr);
        logo += padVersionStr.toString();
        logo += "\n\n";

        genLog.info(logo);

        if (cfg.getConsensusType().equals(CfgConsensus.ConsensusType.POW)) {
            AionPOWChainRunner.start(cfg, sslPass, genLog);
        }
    }

    private static char[] getSslPassword(CfgAion cfg) {
        CfgSsl sslCfg = cfg.getApi().getRpc().getSsl();
        char[] sslPass = sslCfg.getPass();
        // interactively ask for a password for the ssl file if they did not set on in the config file
        if (sslCfg.getEnabled() && sslPass == null) {
            Console console = System.console();
            // https://docs.oracle.com/javase/10/docs/api/java/io/Console.html
            // if the console does not exist, then either:
            // 1) jvm's underlying platform does not provide console
            // 2) process started in non-interactive mode (background scheduler, redirected output, etc.)
            // don't wan't to compromise security in these scenarios
            if (console == null) {
                System.out.println("SSL-certificate-use requested with RPC server and no console found. " +
                        "Please set the ssl password in the config file (insecure) to run kernel non-interactively with this option.");
                System.exit(1);
            } else {
                console.printf("---------------------------------------------\n");
                console.printf("----------- INTERACTION REQUIRED ------------\n");
                console.printf("---------------------------------------------\n");
                sslPass = console.readPassword("Password for SSL keystore file ["
                        +sslCfg.getCert()+"]\n");
            }
        }

        return sslPass;
    }
}