package org.spark.test.other;

public class DoubleDispatch {
	public static void main(String[] args) {
		Shape square = new Square();
		Shape circle = new Circle();
	
		circle.intersects(square);
		circle.intersects(circle);
		square.intersects(circle);
	}
	
	
	static abstract class Shape {
		public abstract void intersects(Shape shape);
		
		protected void intersectsWith(Shape shape) {
//			System.out.println("Shape with shape");
		}
		
		protected void intersectsWith(Circle circle) {
//			System.out.println("Shape with circle");
		}

		protected void intersectsWith(Square square) {
//			System.out.println("Shape with square");
		}
	}
	
	
	static class Circle extends Shape {
		public void intersects(Shape shape) {
			shape.intersectsWith(this);
		}
		
		protected void intersectsWith(Circle circle) {
//			System.out.println("Circle with circle");
		}

		protected void intersectsWith(Square square) {
//			System.out.println("Circle with square");
		}
	}
	
	
	static class Square extends Circle {
		public void intersects(Shape shape) {
			shape.intersectsWith(this);
		}
		
		protected void intersectsWith(Circle circle) {
//			System.out.println("Square with circle");
		}
	}
}
