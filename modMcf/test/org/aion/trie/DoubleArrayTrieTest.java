package org.aion.trie;

import junitparams.JUnitParamsRunner;
import org.aion.mcf.trie.doubleArrayTrie.DATImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RunWith(JUnitParamsRunner.class)
public class DoubleArrayTrieTest {

    DATImpl trie = new DATImpl(17);
    private static final int SEED = 1;
    private Random rnd = new Random(SEED);

    protected String getRandomString() {
        String KEYCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder key = new StringBuilder();

        while (key.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * KEYCHARS.length());
            key.append(KEYCHARS.charAt(index));
        }
        String ketStr = key.toString();
        return ketStr;

    }

    private Map<String, String> getSampleData(int sampleSize) {
        Map<String, String> sampleDataMap = new HashMap<>();

        for(int i=0;i<sampleSize; i++) {
            sampleDataMap.put(getRandomString(), getRandomString());
        }
        return sampleDataMap;
    }

    @Test
    public void checkDeterministicRoot(){
        // for the same input the root should be the same
    }

    @Test
    public void checkHashPropagation(){
        // check that for a given n numbers of key value pairs when we insert a new pair the hash propagates up to the root
    }

    @Test
    public void checkUpdatePropagatesRootHashChanges(){
        // update an existing value and check that the changes propagate to the root
    }

    @Test
    public void checkDeleteRootHashPropagation(){
        // delete an existing node and make sure that the changes propagate up to the root
    }
    
    @Test
    public void checkDeleteRootHashCorrectness(){
        // add two nodes delete one, check that the root matches when we only had one node.
    }

    @Test
    public void insertTest(){
        Map<String, String> sampleDataMap = getSampleData(2);

        // update all sample elements in trie
        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.addToTrie(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            Assert.assertEquals(entry.getValue(), new String(trie.get(entry.getKey().getBytes())));
        }
    }

    @Test
    public void updateTest() {
        Map<String, String> sampleDataMap = getSampleData(2);

        // insert all sample elements into trie
        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.addToTrie(entry.getKey(), entry.getKey());
        }

        // update all sample elements in trie
        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.addToTrie(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            Assert.assertEquals(entry.getValue(), new String(trie.get(entry.getKey().getBytes())));
        }
    }

    @Test
    public void deleteTest() {
        Map<String, String> sampleDataMap = getSampleData(2);

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.addToTrie(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.addToTrie(entry.getKey(), "");
        }

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            Assert.assertEquals("", new String(trie.get(entry.getKey().getBytes())));
        }
    }
}
