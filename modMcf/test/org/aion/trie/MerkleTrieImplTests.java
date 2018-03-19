package org.aion.trie;

import junitparams.JUnitParamsRunner;
import org.aion.mcf.trie.merkle.MerkleTrieImpl;
import org.aion.mcf.trie.doublearray.DATImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RunWith(JUnitParamsRunner.class)
public class MerkleTrieImplTests {
    private static final int SEED = 1;
    private Random rnd = new Random(SEED);

    MerkleTrieImpl merkleTrie = new MerkleTrieImpl(null);
    DATImpl datTrie = new DATImpl(17);


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

    @Test
    public void testHashMatches(){
        Map<String, String> sampleDataMap = new HashMap<>();
        sampleDataMap.put("b", "as");
        sampleDataMap.put("a", "af");

        // insert all sample elements into trie
        for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
            merkleTrie.update(entry.getKey(), entry.getValue());
            datTrie.update(entry.getKey().getBytes(), entry.getValue().getBytes());
        }

        byte[] hash1 = merkleTrie.getRootHash();
        byte[] hash2 = datTrie.getRootHash();
        if(Arrays.equals(hash1, hash2))
        {
            System.out.println("The root hashes match from teh two tries implementations");
        }

    }
}
