package io.vertx.example.unit.test;

import io.vertx.example.web.proxy.locator.CyclicSharedPool;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.text.*;
import java.util.*;

@RunWith(VertxUnitRunner.class)
class CyclicSharePoolTest implements Runnable
{
    private final String                    opcode;
    private final Date                      date;

    CyclicSharePoolTest(String opc) {
        opcode    =opc;
        date      =new Date();
    }

    public void run() {
        if     (opcode.equalsIgnoreCase("Pool"  )) { testPool  (); }
        else if(opcode.equalsIgnoreCase("Sync"  )) { testSync  (); }
        else if(opcode.equalsIgnoreCase("Create")) { testCreate(); }
        else if(opcode.equalsIgnoreCase("Clone" )) { testClone (); }
    }

    @Test
    public void testPool() {
        for(int xa=0; xa<ITERATIONS; xa++) {
            DateFormat fmt=dtsPool.get();
            synchronized(fmt) { dateTimeResult=fmt.format(date); }
        }
    }

    @Test
    public void testSync() {
        for(int xa=0; xa<ITERATIONS; xa++) {
            DateFormat fmt=DTSFMT;
            synchronized(fmt) { dateTimeResult=fmt.format(date); }
        }
    }

    @Test
    public void testCreate() {
        for(int xa=0; xa<ITERATIONS; xa++) {
            DateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dateTimeResult=fmt.format(date);
        }
    }

    @Test
    public void testClone() {
        for(int xa=0; xa<ITERATIONS; xa++) {
            DateFormat fmt=(DateFormat)DTSFMT.clone();
            dateTimeResult=fmt.format(date);
        }
    }

// *************************************************************************************************
// STATIC PROPERTIES
// *************************************************************************************************

    static private final int                MAX_THREADS=10;
    static private final int                ITERATIONS =1000000;

    static private final PrintWriter        SYSOUT=new PrintWriter(System.out,true);
    static private final DateFormat         DTSFMT=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static private final NumberFormat       NBRFMT=NumberFormat.getNumberInstance();

    static private final CyclicSharedPool<DateFormat> dtsPool;                 // date/time stamp formatter pool

    static {
        SimpleDateFormat[] arr=new SimpleDateFormat[MAX_THREADS];
        SimpleDateFormat   tpt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        for(int xa=0; xa<arr.length; xa++) { arr[xa]=(SimpleDateFormat)tpt.clone(); }
        dtsPool=new CyclicSharedPool<DateFormat>(Arrays.asList(arr));
    }

    static public volatile String           dateTimeResult;                                             // prevent test bodies from being optimized away

// *************************************************************************************************
// STATIC METHODS
// *************************************************************************************************

    static private void   println     ()           { SYSOUT.println();                                          }
    static private void   println     (Object obj) { SYSOUT.println(dateTime()+" : "+obj);                      }
    static private String dateTime    ()           { synchronized(DTSFMT) { return DTSFMT.format(new Date()); } }
    static private String formatNumber(long val)   { synchronized(NBRFMT) { return NBRFMT.format(val);        } }
    static private String formatNumber(double val) { synchronized(NBRFMT) { return NBRFMT.format(val);        } }

    static public void main(String[] args) {
        println("Threads="+formatNumber(MAX_THREADS)+", Iterations="+formatNumber(ITERATIONS));

        for(int xa=0; xa<3; xa++) {                                                                     // repeat to warmup JVM/JIT
            println("Test "+(xa+1)+":");
            runTest("Sync"  );
            runTest("Create");
            runTest("Clone" );
            runTest("Pool"  );
        }
    }

    static private void runTest(String opc) {
        Thread[]                            thds=new Thread[MAX_THREADS];
        long                                start;

        for(int xa=0; xa<thds.length; xa++) { thds[xa]=new Thread(new CyclicSharePoolTest(opc)); }
        start=System.currentTimeMillis();
        for(int xa=0; xa<thds.length; xa++) { thds[xa].start();                               }
        for(int xa=0; xa<thds.length; xa++) { waitFor(thds[xa]);                              }
        println("  "+(opc+"          ").substring(0,10)+": "+formatNumber(System.currentTimeMillis()-start)+" ms");

        for(int xa=0; xa<5; xa++) {
            System.gc();
            try { Thread.sleep(200); } catch(InterruptedException thr) { throw new RuntimeException("Interrupted??",thr); }
        }
    }

    static private void waitFor(Thread thd) {
        try { thd.join(); } catch(InterruptedException thr) { throw new RuntimeException("Interrupted??",thr); }
    }

} // END CLASS