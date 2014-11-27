package com.cocobabys.bean;


public class AblumInfo {
	private String dirName = "";
	private String lastestPicPath = "";
	private int dirCount = 0;

	public int getDirCount() {
		return dirCount;
	}

	public void setDirCount(int dirCount) {
		this.dirCount = dirCount;
	}

	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	public String getLastestPicPath() {
		return lastestPicPath;
	}

	public void setLastestPicPath(String lastestPicPath) {
		this.lastestPicPath = lastestPicPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dirName == null) ? 0 : dirName.hashCode());
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
		AblumInfo other = (AblumInfo) obj;
		if (dirName == null) {
			if (other.dirName != null)
				return false;
		} else if (!dirName.equals(other.dirName))
			return false;
		return true;
	}
}
