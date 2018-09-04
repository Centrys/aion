module aion.zero.impl {
    requires aion.base;
    requires aion.mcf;
    requires aion.log;
    requires aion.p2p;
    requires aion.p2p.impl;
    requires aion.rlp;
    requires aion.evtmgr;
    requires aion.evtmgr.impl;
    requires aion.txpool;
    requires aion.crypto;
    requires aion.db.impl;
    requires aion.zero;
    requires aion.vm;
    requires aion.precompiled;
    requires aion.fastvm;
    requires jdk.management;
    requires java.xml;
    requires slf4j.api;

    exports org.aion.equihash;
    exports org.aion.engine.impl;
    exports org.aion.zero.impl.blockchain;
    exports org.aion.zero.impl;
    exports org.aion.zero.impl.core;
    exports org.aion.zero.impl.types;
    exports org.aion.zero.impl.config;
    exports org.aion.zero.impl.cli;
    exports org.aion.zero.impl.db;
    exports org.aion.zero.impl.sync;
    exports org.aion.zero.impl.config.dynamic;
}
