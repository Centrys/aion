package org.aion.trie;

import junitparams.JUnitParamsRunner;
import org.aion.mcf.trie.doublearray.DoubleArrayTrieImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RunWith(JUnitParamsRunner.class)
public class DoubleArrayTrieTest {

    DoubleArrayTrieImpl trie = new DoubleArrayTrieImpl();
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
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        byte[] firstRoothash = trie.getRootHash();
        DoubleArrayTrieImpl trie2 = new DoubleArrayTrieImpl();

        for(Map.Entry<String, String> entry : data.entrySet()){
            trie2.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        byte[] secondRootHash = trie2.getRootHash();

        Assert.assertEquals(true, Arrays.equals(firstRoothash, secondRootHash));
    }

    @Test
    public void checkHashPropagation(){

        for(Map.Entry<String, String> entry : getSampleData(10).entrySet()){
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        byte[] rootHashIntermediary = trie.getRootHash();

        for(Map.Entry<String, String> entry : getStaticSampleData().entrySet()){
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        byte[] rootHashFinal = trie.getRootHash();

        Assert.assertEquals(false, Arrays.equals(rootHashFinal, rootHashIntermediary));

    }

    @Test
    public void checkUpdatePropagatesRootHashChanges(){
        for(Map.Entry<String, String> entry : getStaticSampleData().entrySet()){
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        byte[] tempRootHash = trie.getRootHash();

        trie.update("key1".getBytes(), "value1New".getBytes());

        byte[] newRootHash = trie.getRootHash();

        Assert.assertEquals(false, Arrays.equals(tempRootHash, newRootHash));

    }

    @Test
    public void checkDeleteRootHashPropagation(){
        for(Map.Entry<String, String> entry : getStaticSampleData().entrySet()){
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        byte[] tempRootHash = trie.getRootHash();

        trie.update("key1".getBytes(), "".getBytes());

        byte[] newRootHash = trie.getRootHash();

        Assert.assertEquals(false, Arrays.equals(tempRootHash, newRootHash));
    }

    @Test
    public void checkDeleteRootHashCorrectness(){
        trie.update("key1".getBytes(), "value1".getBytes());
        byte[] tempRootHash = trie.getRootHash();
        trie.update("key2".getBytes(), "value2".getBytes());
        trie.update("key2".getBytes(), "".getBytes());
        byte[] newRootHash = trie.getRootHash();

        Assert.assertEquals(true, Arrays.equals(tempRootHash, newRootHash));


    }

    @Test
    public void insertTest(){
        Map<String, String> sampleDataMap = getSampleData(2);

        // update all sample elements in trie
        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
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
            trie.update(entry.getKey().getBytes(), entry.getKey().getBytes());
        }

        // update all sample elements in trie
        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            Assert.assertEquals(entry.getValue(), new String(trie.get(entry.getKey().getBytes())));
        }
    }

    @Test
    public void deleteTest() {
        Map<String, String> sampleDataMap = getSampleData(2);

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            trie.update(entry.getKey().getBytes(), "".getBytes());
        }

        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            Assert.assertEquals("", new String(trie.get(entry.getKey().getBytes())));
        }
    }
}
