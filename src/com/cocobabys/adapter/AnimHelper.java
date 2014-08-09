package com.cocobabys.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import com.cocobabys.R;
import com.cocobabys.media.MediaMgr;

public class AnimHelper {

	private AnimationDrawable anim = null;
	private ImageView currentPlayAnimView;
	boolean bLeft = false;

	public AnimHelper(ImageView currentPlayAnimView, boolean bLeft) {
		this.currentPlayAnimView = currentPlayAnimView;
		this.bLeft = bLeft;
	}

	public boolean isSameView(ImageView imageView) {
		return currentPlayAnimView.equals(imageView);
	}

	public void stopAnimation() {
		if (anim != null && anim.isRunning()) {
			anim.stop();
			if (bLeft) {
				currentPlayAnimView.setBackgroundResource(R.drawable.playing_3);
			} else {
				currentPlayAnimView
						.setBackgroundResource(R.drawable.playing_3_r);
			}
		}
		MediaMgr.close();
	}

	public void startAnimation() {
		if (bLeft) {
			currentPlayAnimView.setBackgroundResource(R.anim.anim_l);
		} else {
			currentPlayAnimView.setBackgroundResource(R.anim.anim_r);
		}
		anim = (AnimationDrawable) currentPlayAnimView.getBackground();
		anim.stop();
		anim.start();
	}

}