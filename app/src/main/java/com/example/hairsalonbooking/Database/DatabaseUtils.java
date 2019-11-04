package com.example.hairsalonbooking.Database;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Interface.ICartItemLoadListener;
import com.example.hairsalonbooking.Interface.ICountItemCartListener;
import com.example.hairsalonbooking.Interface.ISumCartListener;

import java.util.List;

public class DatabaseUtils {


    public static void sumCart(CartDatabase db, ISumCartListener iSumCartListener){
        SumCartAsync task = new SumCartAsync(db, iSumCartListener);
        task.execute();

    }


    public static void getAllCart(CartDatabase db, ICartItemLoadListener iCartItemLoadListener){
        GetAllCartAsync task  = new GetAllCartAsync(db,iCartItemLoadListener);
        task.execute();

    }

    public static void updateCart(CartDatabase db, CartItem cartItem){
        UpdateCartAsync task = new UpdateCartAsync(db);
        task.execute(cartItem);
    }


    public static void insertToCart(CartDatabase db, CartItem... cartItems) {
        InsertToCartAsync task = new InsertToCartAsync(db);
        task.execute(cartItems);
    }

    public static void countItemInCart(CartDatabase db, ICountItemCartListener iCountItemCartListener) {
        CountItemInCartAsync task = new CountItemInCartAsync(db, iCountItemCartListener);
        task.execute();
    }




    private static class UpdateCartAsync extends AsyncTask<CartItem, Void, Void> {
        private final CartDatabase db;

        public UpdateCartAsync(CartDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(CartItem... cartItems) {
            db.cartDAO().update(cartItems[0]);
            return null;
        }
    }

    private static class SumCartAsync extends AsyncTask<Void,Void,Long>{
        private final CartDatabase db;
        private final ISumCartListener listener;

        public SumCartAsync(CartDatabase db, ISumCartListener listener) {
            this.db = db;
            this.listener = listener;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            return db.cartDAO().sumPrice(Common.currentUser.getPhoneNumber());

        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            listener.onSumCartSuccess(aLong);
        }
    }

    private static class GetAllCartAsync extends AsyncTask<String, Void, List<CartItem>> {
        CartDatabase db;
        ICartItemLoadListener listener;
        public GetAllCartAsync(CartDatabase cartDatabase, ICartItemLoadListener iCartItemLoadListener) {
            db = cartDatabase;
            listener = iCartItemLoadListener;
        }
        @Override
        protected List<CartItem> doInBackground(String... strings) {
            return db.cartDAO().getAllItemFromCart(Common.currentUser.getPhoneNumber());
        }

        @Override
        protected void onPostExecute(List<CartItem> cartItems) {
            super.onPostExecute(cartItems);
            listener.onGetAllItemFromCartSuccess(cartItems);
        }
    }


    private static class InsertToCartAsync extends AsyncTask<CartItem, Void, Void> {


        CartDatabase db;

        public InsertToCartAsync(CartDatabase cartDatabase) {
            db = cartDatabase;
        }

        @Override
        protected Void doInBackground(CartItem... cartItems) {
            insertToCart(db, cartItems[0]);
            return null;
        }

        private void insertToCart(CartDatabase db, CartItem cartItem) {
            //If item already avaiable in cart ; just increase quantity
            try {
                db.cartDAO().insert(cartItem);
            } catch (SQLiteConstraintException e) {
                CartItem updateCartItem = db.cartDAO().getProductInCart(cartItem.getProductId(), Common.currentUser.getPhoneNumber());
                updateCartItem.setProductQuantity(updateCartItem.getProductQuantity() + 1);
                db.cartDAO().update(updateCartItem);
            }
        }


    }


    private static class CountItemInCartAsync extends AsyncTask<Void, Void,Integer> {

    CartDatabase db;
    ICountItemCartListener listener;
    public CountItemInCartAsync(CartDatabase cartDatabase, ICountItemCartListener iCountItemCartListener) {
        db = cartDatabase;
        listener = iCountItemCartListener;
    }


        @Override
        protected Integer doInBackground(Void... voids) {
            return Integer.parseInt(String.valueOf(countItemInCartRun(db)));
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            listener.onCartItemCountSuccess(integer.intValue());
        }

        private int countItemInCartRun(CartDatabase db) {
            return db.cartDAO().countItemInCart(Common.currentUser.getPhoneNumber());
        }
    }
}
