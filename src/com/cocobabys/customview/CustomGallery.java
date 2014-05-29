package com.cocobabys.customview;

public class CustomGallery {

	private String sdcardPath;
	private boolean isSeleted = false;

	public String getSdcardPath() {
		return sdcardPath;
	}

	public void setSdcardPath(String sdcardPath) {
		this.sdcardPath = sdcardPath;
	}

	public boolean isSeleted() {
		return isSeleted;
	}

	public void setSeleted(boolean isSeleted) {
		this.isSeleted = isSeleted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sdcardPath == null) ? 0 : sdcardPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomGallery other = (CustomGallery) obj;
		if (sdcardPath == null) {
			if (other.sdcardPath != null)
				return false;
		} else if (!sdcardPath.equals(other.sdcardPath))
			return false;
		return true;
	}

}
