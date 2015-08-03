package com.cocobabys.bean;

public class ActionInfo extends BusinessInfo {
	private Price price;

	public Price getPrice() {
		return price;
	}

	public void setPrice(Price price) {
		this.price = price;
	}

	public static class Price {
		private double origin;
		private double discounted;

		public double getOrigin() {
			return origin;
		}

		public void setOrigin(double origin) {
			this.origin = origin;
		}

		public double getDiscounted() {
			return discounted;
		}

		public void setDiscounted(double discounted) {
			this.discounted = discounted;
		}

	}

}