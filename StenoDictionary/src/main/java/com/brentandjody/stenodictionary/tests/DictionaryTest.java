package com.brentandjody.stenodictionary.tests;

import android.test.AndroidTestCase;
import android.util.Log;
import android.widget.ProgressBar;

import com.brentandjody.stenodictionary.Dictionary;

import junit.framework.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;


public class DictionaryTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String[] files = new String[] {"test.json", "test2.json"};
        for (String file : files) {
            try {
                InputStream in = getContext().getAssets().open(file);
                File outFile = new File("/sdcard", file);
                OutputStream out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + file, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        String[] files = new String[] {"test.json", "test2.json", "test3.json"};
        for (String file : files) {
            try {
                File outFile = new File("/sdcard", file);
                outFile.delete();
            } catch(Exception e) {
                Log.e("tag", "Failed to delete temporary dictionary: " + file, e);
            }
        }
    }

    public void testAPP_HAS_WRITE_PERMISSION() throws Exception {
        Dictionary dictionary = new Dictionary(getContext());
        final CountDownLatch latch = new CountDownLatch(1);
        dictionary.load(new String[] {"/sdcard/test.json"}, null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch.countDown();
            }
        });
        latch.await();
        assertTrue(dictionary.size() > 0);
    }

    public void testBadFilename() throws Exception {
        Dictionary dictionary = new Dictionary(getContext());
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        dictionary.load(new String[] {"booga.json"},null, new ProgressBar(getContext()), 0);
        assertEquals("Dictionary File: booga.json could not be found", errContent.toString().trim());
    }

    public void testIllegalFileType() throws Exception{
        Dictionary dictionary = new Dictionary(getContext());
        try {
            dictionary.load(new String[] {"test.rtf"}, null, new ProgressBar(getContext()), 0);
            Assert.fail("Illegal file type");
        } catch (Exception e) {
        }
    }

    public void testDefaultDictionary() throws Exception{
        Dictionary dictionary = new Dictionary(getContext());
        final CountDownLatch latch = new CountDownLatch(1);
        assertEquals(0, dictionary.size());
        dictionary.load(new String[0], getContext().getAssets(), new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch.countDown();
            }
        });
        latch.await();
        int size = dictionary.size();
        assertTrue(size > 0);
        assertEquals("PHA*T/PHAT/EUBG", dictionary.lookup("mathematic").peek() );
    }

    public void testLoadAndClear() throws Exception {
        Dictionary dictionary = new Dictionary(getContext());
        final CountDownLatch latch = new CountDownLatch(1);
        assertEquals(0, dictionary.size());
        dictionary.load(new String[] {"/sdcard/test.json"}, null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch.countDown();
            }
        });
        latch.await();
        int size = dictionary.size();
        assertTrue(size > 0);
        dictionary.unload();
        assertEquals(0, dictionary.size());
    }

    public void testOverrideEntries() throws Exception{
        Dictionary dictionary = new Dictionary(getContext());
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch latch1 = new CountDownLatch(1);
        assertEquals(0,dictionary.size());
        dictionary.load(new String[] {"/sdcard/test.json"}, null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch.countDown();
            }
        });
        latch.await();
        final int size = dictionary.size();
        final int answer_size = dictionary.lookup("adjudicator").size();
        assertTrue(size > 0);
        assertEquals("AD/SKWRAOUD/KAEU/TOR", dictionary.lookup("adjudicator").peek());
        dictionary.load((new String[] {"/sdcard/test.json", "/sdcard/test2.json"}), null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch1.countDown();
            }
        });
        latch1.await();
        assertEquals("AD/SKWRAOUD/KAEU/T", dictionary.lookup("adjudicator").peek());
        assertEquals(answer_size+1, dictionary.lookup("adjudicator").size());
    }

    public void testLoad2Dictionaries() throws Exception{
        Dictionary dictionary = new Dictionary(getContext());
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch latch1 = new CountDownLatch(1);
        assertEquals(0,dictionary.size());
        dictionary.load(new String[] {"/sdcard/test.json"}, null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch.countDown();
            }
        });
        latch.await();
        int size = dictionary.size();
        assertTrue(size > 0);
        dictionary.load(new String[] {"/sdcard/test.json", "/sdcard/test3.json"}, null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch1.countDown();
            }
        });
        latch1.await();
        assertTrue(dictionary.size() == size);
    }

    public void testCaseInsensitivity() throws Exception{
        Dictionary dictionary = new Dictionary(getContext());
        final CountDownLatch latch = new CountDownLatch(1);
        assertEquals(0, dictionary.size());
        dictionary.load(new String[]{"/sdcard/test.json"}, null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch.countDown();
            }
        });
        latch.await();
        assertEquals("AD/SKWRUR", dictionary.lookup("adjure").peek());
        assertEquals("AD/SKWRUR", dictionary.lookup("Adjure").peek());
        assertEquals("AD/SKWRUR", dictionary.lookup("ADJURE").peek());
    }

    public void testCurlyBrackets() throws Exception{
        Dictionary dictionary = new Dictionary(getContext());
        final CountDownLatch latch = new CountDownLatch(1);
        assertEquals(0, dictionary.size());
        dictionary.load(new String[]{"/sdcard/test.json"}, null, new ProgressBar(getContext()), 0);
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
                latch.countDown();
            }
        });
        latch.await();
        assertEquals(1, dictionary.possibilities("{^ful}", 10).size());
        Queue result = dictionary.possibilities("{^ful}", 10);
        assertEquals(result, dictionary.possibilities("^ful", 10));
    }

}
