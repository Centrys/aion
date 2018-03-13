package org.aion.trie;

import com.google.common.base.Stopwatch;
import junitparams.JUnitParamsRunner;
import org.aion.base.util.Hex;
import org.aion.mcf.trie.TrieImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.aion.crypto.HashUtil.EMPTY_TRIE_HASH;

@RunWith(JUnitParamsRunner.class)
public class TestTrieBenchmark {
    private static final boolean BENCHMARK_ENABLED = true;

    private static String ROOT_HASH_EMPTY = Hex.toHexString(EMPTY_TRIE_HASH);

    private static final String SAMPLE_FILE = "samples/sample4.dat";

    TrieImpl trie = new TrieImpl(null);

    private Map<String, String> readSampleData() {
        Map<String, String> sampleDataMap = new HashMap<>();

        Path filePath = FileSystems.getDefault().getPath(SAMPLE_FILE);
        try (Stream<String> stream = Files.lines(filePath)) {
            stream.forEach(s -> {
                String[] keyValue = s.split(" ");

                if (keyValue.length == 2) {
                    sampleDataMap.put(keyValue[0], keyValue[1]);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sampleDataMap;
    }

    @Test
    public void benchmarkInsert() {
        if (BENCHMARK_ENABLED) {
            Map<String, String> sampleDataMap = readSampleData();
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
            Map<String, String> sampleDataMap = readSampleData();

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
            Map<String, String> sampleDataMap = readSampleData();

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
            Map<String, String> sampleDataMap = readSampleData();

            // create a new trie object without database
            //TrieImpl trie = new TrieImpl(null);

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
