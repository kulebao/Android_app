package com.cocobabys.bean;

public class BusinessSummary {
	private Contractor contractor = new Contractor();
	private Activity activity = new Activity();

	public boolean isValid() {
		return contractor.isValid() && activity.isValid();
	}

	public Contractor getContractor() {
		return contractor;
	}

	public void setContractor(Contractor contractor) {
		this.contractor = contractor;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public static class Contractor {
		private int threshold = 0;
		private int current = -1;

		public int getThreshold() {
			return threshold;
		}

		public void setThreshold(int threshold) {
			this.threshold = threshold;
		}

		public int getCurrent() {
			return current;
		}

		public void setCurrent(int current) {
			this.current = current;
		}

		boolean isValid() {
			return current >= threshold;
		}
	}

	public static class Activity {
		private int threshold = 0;
		private int current = -1;

		public int getThreshold() {
			return threshold;
		}

		public void setThreshold(int threshold) {
			this.threshold = threshold;
		}

		public int getCurrent() {
			return current;
		}

		public void setCurrent(int current) {
			this.current = current;
		}

		boolean isValid() {
			return current >= threshold;
		}
	}
}
