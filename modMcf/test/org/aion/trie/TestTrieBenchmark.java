package org.aion.trie;

import com.google.common.base.Stopwatch;
import junitparams.JUnitParamsRunner;
import org.aion.mcf.trie.TrieImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RunWith(JUnitParamsRunner.class)
public class TestTrieBenchmark {
    private static final boolean BENCHMARK_ENABLED = true;

    private static final int SAMPLE_LENGHT = 100000;
    private static final int SEED = 1;
    private Random rnd = new Random(SEED);

    TrieImpl trie = new TrieImpl(null);

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
    public void benchmarkInsert() {
        if (BENCHMARK_ENABLED) {
            Map<String, String> sampleDataMap = getSampleData(SAMPLE_LENGHT);
            Stopwatch stopwatch = Stopwatch.createStarted();

            // insert all sample elements into trie
            for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
                trie.update(entry.getKey(), entry.getValue());
            }

            stopwatch.stop();

            long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Insert duration: " + millis + "ms");
        }
    }

    @Test
    public void benchmarkRead() {
        if (BENCHMARK_ENABLED) {
            Map<String, String> sampleDataMap = getSampleData(SAMPLE_LENGHT);

            // insert all sample elements into trie
            for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
                trie.update(entry.getKey(), entry.getValue());
            }

            Stopwatch stopwatch = Stopwatch.createStarted();

            // update all sample elements in trie
            for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
                Assert.assertEquals(entry.getValue(), new String(trie.get(entry.getKey().getBytes())));
            }

            stopwatch.stop();

            long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Read duration: " + millis + "ms");
        }
    }

    @Test
    public void benchmarkUpdate() {
        if (BENCHMARK_ENABLED) {
            Map<String, String> sampleDataMap = getSampleData(SAMPLE_LENGHT);

            // insert all sample elements into trie
            for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
                trie.update(entry.getKey(), entry.getKey());
            }

            Stopwatch stopwatch = Stopwatch.createStarted();

            // update all sample elements in trie
            for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
                trie.update(entry.getKey(), entry.getValue());
            }

            stopwatch.stop();

            long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Update duration: " + millis + "ms");
        }
    }

    @Test
    public void benchmarkDelete() {
        if (BENCHMARK_ENABLED) {
            Map<String, String> sampleDataMap = getSampleData(SAMPLE_LENGHT);

            // insert all sample elements into trie
            for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
                trie.update(entry.getKey(), entry.getKey());
            }

            Stopwatch stopwatch = Stopwatch.createStarted();

            // update all sample elements in trie
            for (Map.Entry<String, String> entry : sampleDataMap.entrySet()) {
                trie.update(entry.getKey(), "");
            }

            stopwatch.stop();

            //assertThat(Hex.toHexString(trie.getRootHash())).isEqualTo(ROOT_HASH_EMPTY);

            long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Delete duration: " + millis + "ms");
        }
    }
}
