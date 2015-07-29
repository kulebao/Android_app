package com.cocobabys.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.cocobabys.R;
import com.cocobabys.fragment.ActionFragment;
import com.cocobabys.fragment.ShopFragment;

public class BusinessActivity extends FragmentActivity{
    private static final int ACTION       = 0;
    private static final int MERCHANT     = 1;

    private int              currentIndex = ACTION;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business);

        Log.d("", "MainActivity action");
        moveToAction();
    }

    private void moveToAction(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ActionFragment fragment = new ActionFragment();
        transaction.replace(R.id.djc_fragment, fragment);
        transaction.commit();
    }

    private void moveToShop(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ShopFragment fragment = new ShopFragment();
        transaction.replace(R.id.djc_fragment, fragment);
        transaction.commit();
    }

    public void action(View view){
        Log.d("", "action currentIndex=" + currentIndex);
        if(currentIndex == ACTION){
            return;
        }

        moveToAction();
        currentIndex = ACTION;
    }

    public void shop(View view){
        Log.d("", "shop currentIndex=" + currentIndex);

        if(currentIndex == MERCHANT){
            return;
        }

        moveToShop();
        currentIndex = MERCHANT;
    }
}
