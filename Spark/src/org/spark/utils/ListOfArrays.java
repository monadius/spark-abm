package org.spark.utils;

import java.util.ArrayList;

/**
 * Can be used instead of ArrayList
 * @author Monad
 *
 * @param <T>
 */
public class ListOfArrays<T> {
	/* List of arrays */
	private final ArrayList<T[]> list;
	
	/* Number of all elements */
	private int size;
	
	/* Reference to the current array */
	private T[] currentArray;
	
	/* Index of the last element in the current array */
	private int currentIndex;
	
	/* Index of the current array inside the list */
	private int currentArrayNumber;
	
	/* Default capacity of each array */
	private final static int BIN_CAPACITY = 10;
	private final static int CAPACITY = 1 << BIN_CAPACITY;
	
	/**
	 * Creates a list of arrays with the given initial capacity
	 * @param capacity
	 */
	public ListOfArrays(int capacity) {
		if (capacity < 0)
			capacity = 0;
		
		int numberOfArrays = (capacity >>> BIN_CAPACITY) + 1;

		list = new ArrayList<T[]>(numberOfArrays);
		init(numberOfArrays);
	}
	
	
	/**
	 * The default constructor
	 */
	public ListOfArrays() {
		this(1);
	}
	
	
	/**
	 * Initializes the collection
	 * @param numberOfArrays
	 */
	@SuppressWarnings("unchecked")
	protected void init(int numberOfArrays) {
		if (numberOfArrays < 1)
			numberOfArrays = 1;
		
		for (int i = 0; i < numberOfArrays; i++) {
			list.add((T[]) new Object[CAPACITY]);
		}
		
		currentArray = list.get(0);
		currentArrayNumber = 0;
		currentIndex = 0;
		size = 0;
	}
	
	/**
	 * Increases the overall capacity by creating a new array
	 * if necessary
	 */
	@SuppressWarnings("unchecked")
	protected void increaseCapacity() {
		if (currentIndex < CAPACITY)
			return;
		
		if (currentArrayNumber == list.size() - 1) {
			currentArrayNumber++;
			currentArray = (T[]) new Object[CAPACITY];
			list.add(currentArray);
			
			currentIndex = 0;
		}
		else {
			currentArrayNumber++;
			currentArray = list.get(currentArrayNumber);
			currentIndex = 0;
		}
	}
	
	
	/**
	 * Adds the element to the end of the collection
	 * @param element
	 */
	public void add(T element) {
		if (currentIndex >= CAPACITY) {
			increaseCapacity();
		}
		
		currentArray[currentIndex++] = element;
		size++;
	}
	
	
	/**
	 * Returns the element at the position specified by the index
	 * @param index
	 * @return
	 */
	public T get(int index) {
		if (index >= size) {
		    throw new IndexOutOfBoundsException(
		    		"Index: " + index + ", Size: " + size);
		}
		
		int arrayIndex = index >>> BIN_CAPACITY;
		index -= arrayIndex << BIN_CAPACITY;
		
		T[] array = list.get(arrayIndex);
		return array[index];
	}
	
	
	
	/**
	 * Removes all elements from the collection
	 */
	public void clear() {
		list.clear();
		init(1);
	}
}
