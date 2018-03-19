package org.aion.trie;

import junitparams.JUnitParamsRunner;
import org.aion.mcf.trie.doubleArrayTrie.DATImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
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

    private Map<String, String> getStaticSampleData(){
        Map<String, String> sampleDataMap = new HashMap<>();

        sampleDataMap.put("key1", "value1");
        sampleDataMap.put("key2", "value2");

        return sampleDataMap;
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
        Map<String, String> data = getStaticSampleData();

        for(Map.Entry<String, String> entry : data.entrySet()){
            trie.addToTrie(entry.getKey(), entry.getValue());
        }

        byte[] firstRoothash = trie.getRootHash();
        DATImpl trie2 = new DATImpl(17);

        for(Map.Entry<String, String> entry : data.entrySet()){
            trie2.addToTrie(entry.getKey(), entry.getValue());
        }

        byte[] secondRootHash = trie2.getRootHash();

        Assert.assertEquals(true, Arrays.equals(firstRoothash, secondRootHash));
    }

    @Test
    public void checkHashPropagation(){

        for(Map.Entry<String, String> entry : getSampleData(10).entrySet()){
            trie.addToTrie(entry.getKey(), entry.getValue());
        }

        byte[] rootHashIntermediary = trie.getRootHash();

        for(Map.Entry<String, String> entry : getStaticSampleData().entrySet()){
            trie.addToTrie(entry.getKey(), entry.getValue());
        }

        byte[] rootHashFinal = trie.getRootHash();

        Assert.assertEquals(false, Arrays.equals(rootHashFinal, rootHashIntermediary));

    }

    @Test
    public void checkUpdatePropagatesRootHashChanges(){
        for(Map.Entry<String, String> entry : getStaticSampleData().entrySet()){
            trie.addToTrie(entry.getKey(), entry.getValue());
        }

        byte[] tempRootHash = trie.getRootHash();

        trie.addToTrie("key1", "value1New");

        byte[] newRootHash = trie.getRootHash();

        Assert.assertEquals(false, Arrays.equals(tempRootHash, newRootHash));

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
