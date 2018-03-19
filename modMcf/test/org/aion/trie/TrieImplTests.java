package org.aion.trie;

import com.google.common.base.Stopwatch;
import junitparams.JUnitParamsRunner;
import org.aion.crypto.HashUtil;
import org.aion.mcf.trie.TrieImpl;
import org.aion.mcf.trie.doubleArrayTrie.DATImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RunWith(JUnitParamsRunner.class)
public class TrieImplTests {
    private static final int SEED = 1;
    private Random rnd = new Random(SEED);

    TrieImpl merkleTrie = new TrieImpl(null);
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
            datTrie.addToTrie(entry.getKey(), entry.getValue());
        }

        byte[] hash1 = merkleTrie.getRootHash();
        byte[] hash2 = datTrie.getRootHash();
        if(Arrays.equals(hash1, hash2))
        {
            System.out.println("The root hashes match from teh two tries implementations");
        }

    }
}
