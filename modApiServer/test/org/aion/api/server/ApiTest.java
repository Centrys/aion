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

package org.aion.api.server;

import org.aion.api.server.types.CompiledContr;
import org.aion.base.type.Address;
import org.aion.mcf.account.AccountManager;
import org.aion.mcf.account.Keystore;
import org.aion.mcf.types.AbstractBlock;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ApiTest {

    private class ApiImpl extends Api {

        @Override
        public String getCoinbase() {
            return null;
        }

        @Override
        public byte getApiVersion() {
            return 0;
        }

        @Override
        public AbstractBlock getBestBlock() {
            return null;
        }

        @Override
        public AbstractBlock<?, ?> getBlock(long _bn) {
            return null;
        }

        @Override
        public AbstractBlock<?, ?> getBlockByHash(byte[] hash) {
            return null;
        }

        @Override
        public BigInteger getBalance(String _address) {
            return null;
        }

        private ApiImpl() {
            super(null);
        }
    }

    private ApiImpl api;

    @Before
    public void setup() {
        api = new ApiImpl();
    }

    private static final String KEYSTORE_PATH;
    private String addr;


    static {
        String storageDir = System.getProperty("local.storage.dir");
        if (storageDir == null || storageDir.equalsIgnoreCase("")) {
            storageDir = System.getProperty("user.dir");
        }
        KEYSTORE_PATH = storageDir + "/keystore";
    }

    private void tearDown() {
        // get a list of all the files in keystore directory
        File folder = new File(KEYSTORE_PATH);
        File[] AllFilesInDirectory = folder.listFiles();
        List<String> allFileNames = new ArrayList<>();
        List<String> filesToBeDeleted = new ArrayList<>();

        // check for invalid or wrong path - should not happen
        if(AllFilesInDirectory == null)
            return;

        for(File file: AllFilesInDirectory){
            allFileNames.add(file.getName());
        }

        // get a list of the files needed to be deleted, check the ending of file names
        // with corresponding addresses
        for(String name: allFileNames){
            String ending = name.substring(name.length()-64);

            if(ending.equals(addr)) {
                filesToBeDeleted.add(KEYSTORE_PATH + "/"+ name);
            }
        }

        // iterate and delete those files
        for (String name: filesToBeDeleted){
            File file = new File(name);
            if (file.delete())
                System.out.println("Deleted file: " + name);
        }
    }

    @Test
    public void testLockAndUnlock() {
        assertFalse(api.unlockAccount(Address.ZERO_ADDRESS(), "testPassword", 0));
        assertFalse(api.unlockAccount(Address.ZERO_ADDRESS().toString(), "testPassword", 0));
        assertFalse(api.lockAccount(Address.ZERO_ADDRESS(), "testPassword"));

        addr = Keystore.create("testPwd");
        assertTrue(api.unlockAccount(addr, "testPwd", 50000));
        tearDown();
    }

    @Test
    public void testAccountRetrieval() {
        assertNull(api.getAccountKey(Address.ZERO_ADDRESS().toString()));

        addr = Keystore.create("testPwd");
        assertEquals(AccountManager.inst().getKey(Address.wrap(addr)), api.getAccountKey(addr));

        assertTrue(api.getAccounts().contains(addr));
        tearDown();
    }

    @Test
    public void testCompileFail() {
        String contract = "This should fail\n";
        assertNotNull(api.contract_compileSolidity(contract).get("compile-error"));
    }

    @Test
    public void testCompilePass1() {
        // Taken from FastVM CompilerTest.java
        String contract = "pragma solidity ^0.4.0;\n" + //
                "\n" + //
                "contract SimpleStorage {\n" + //
                "    uint storedData;\n" + //
                "\n" + //
                "    function set(uint x) {\n" + //
                "        storedData = x;\n" + //
                "    }\n" + //
                "\n" + //
                "    function get() constant returns (uint) {\n" + //
                "        return storedData;\n" + //
                "    }\n" + //
                "}";
        Map<String, CompiledContr> compileResult = api.contract_compileSolidity(contract);
        CompiledContr compiledContr = compileResult.get("SimpleStorage");
        assertNull(compiledContr.error);
    }

    @Test
    public void testContractCreateResult() {
        ApiImpl.ContractCreateResult ccr = api.new ContractCreateResult();
        assertNull(ccr.address);
        assertNull(ccr.transId);
    }

}