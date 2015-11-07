package io.vertx.example.web.proxy.locator;

import java.util.List;

/**
 * A pool of identical objects which will be delivered to requesting threads in round-robin order, with minimal
 * synchronization.  The objects being managed are not thread-safe and must be externally synchronized by the calling
 * code. The typical use case is to amortize the synchronization collisions on these objects among some fixed number of
 * pre-created objects.  The objects are typically relatively expensive to create.
 * <p>
 * The usage pattern for this pool is to fetch a reference, synchronize appropriately (usually on the returned reference),
 * use it and release it.  References to pooled objects should not be retained beyond their immediate use.
 * <p>
 * All threads using this object must obtain the reference to it in a thread-safe manner.  Typically, this will be an
 * static final reference created during class initialization.
 * <p>
 * Examples of objects managed by a cyclic pool would be formatters, parsers and pre-compiled regular expression
 * matchers.
 * <p>
 * Example Usage:
 * <p>
 * <pre>
 * static private final CyclicSharePool<SimpleDateFormat> datestampFormatters;
 * static {
 *     SimpleDateFormat[] arr=new SimpleDateFormat[50];;
 *     SimpleDateFormat   tpt=new SimpleDateFormat("yyyyMMddHHmmss");;
 *     for(int xa=0; xa<arr.length; xa++) { arr[xa]=(SimpleDateFormat)tpt.clone(); }
 *     datestampFormatters=new CyclicSharePool(tpt);;
 *     }
 * </pre>
 * <p>
 * Threading Design : [ ] Single Threaded  [x] Threadsafe  [ ] Immutable  [ ] Isolated
 */

public class CyclicSharedPool<T> {
    private volatile Node<T> current;


    public CyclicSharedPool(List<T> objs) {
        if (objs == null || objs.isEmpty() ) {
            throw new IllegalArgumentException("Objects provided for CyclicSharePool cannot be null or zero-length");
        }

        AssignList:
        {
            Node<T> nod = new Node<T>(objs.get(objs.size() - 1), null), las = nod;
            for (int xa = (objs.size() - 2); xa >= 0; xa--) {
                nod = new Node<T>(objs.get(xa), nod);

            }                 // reverse order to supplied order is preserved
            las.next = nod;
            current = nod;
        }
    }

    /**
     * Get the next object from the share-pool.  Note, since share-pools are often used to arbitrate non-threadsafe objects,
     * the returned object must generally be synchronized externally unless you know it is internally synchronized.
     */
    public T get() {
        T obj;

        obj = current.object;
        current = current.next;
        return obj;
    }

// *************************************************************************************************
// STATIC NESTED CLASS
// *************************************************************************************************

    static private class Node<T> extends Object {
        final T object;
        Node<T> next;
      // can't be final else it's not possible to cycle the last node back to the first

        Node(T val, Node<T> nxt) {
            object = val;
            next = nxt;
        }
    }

} // END CLASS