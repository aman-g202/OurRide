package com.ourride.driver.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class Utility {
    static String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";
    private static ProgressDialog dialog;

    public static void loadImage(Context ctx, ImageView imageView, String url) {
        Picasso.get().load(url).into(imageView);
    }

    /*public static void loadSpeakerImage(Context ctx, ImageView imageView, String url) {
        Picasso.with(ctx).load(url).placeholder(R.drawable.logo_hindi).error(R.drawable.logo_hindi).into(imageView);
    }*/

    public static void toastView(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

   /* public static void setFragment(Fragment fragment, Context ctx, String tag) {
        ((HomeActivity) ctx).getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, fragment, tag).commit();
    }

   public static void setFragment1(Fragment fragment, Context ctx, String tag) {
        ((CheckOutActivity) ctx).getSupportFragmentManager().beginTransaction().replace(R.id.viewpager, fragment).addToBackStack(null).commit();
    }


    public static void setFragment2(Fragment fragment, Context ctx, String tag) {
        ((CheckOutActivity) ctx).getSupportFragmentManager().beginTransaction().replace(R.id.viewpager, fragment).commit();
    }*/

   /* public static void setProfileFragment(Fragment fragment, Context ctx) {
        ((MainActivity) ctx).getSupportFragmentManager().beginTransaction().replace(R.id.fl_myprofile, fragment).commit();
    }*/

   /* public static String getTotal(HelperManager helperManager) {
        float total = 0;
        float round_total = 0;
        ArrayList<ProductDetail> total_list = helperManager.readAllCart();
        for (int i = 0; i < total_list.size(); i++) {
            float pr = Float.parseFloat(total_list.get(i).getPrice());
            int qty = total_list.get(i).getQuantity();
            float tot = pr * qty;
            total += tot;
            round_total = Math.round(total);
        }
        return String.valueOf(round_total);
    }

    public static String getCartTotal(DatabaseHandler handler) {
        float total = 0;
        float round_total = 0;
        ArrayList<ProductDetail> total_list = handler.getAllUrlList();
        for (int i = 0; i < total_list.size(); i++) {
            float pr = Float.parseFloat(total_list.get(i).getPrice());
            int qty = total_list.get(i).getQuantity();
            float tot = pr * qty;
            total += tot;
            round_total = Math.round(total);
        }
        return String.valueOf(round_total);
    }*/

    public static void showLoader(Context ctx) {
        dialog = new ProgressDialog(ctx);
        dialog.setCancelable(true);
        dialog.setMessage("loading..");
        dialog.show();
    }

    public static void hideLoader() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static boolean emailCheck(String email) {
        if (email.matches(emailPattern)) {
            return true;
        } else {
            return false;
        }
    }

    public static void shareApp(Context ctx) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Raktdoot");
            String sAux = "Checkout this application, Best App for find your nearest blood donors\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=" + ctx.getPackageName() + "\n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            ctx.startActivity(Intent.createChooser(i, "Select one"));
        } catch (Exception e) {
            //e.toString();
        }
    }

    public static void rateUs(Context ctx) {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ctx.getPackageName()));
        ctx.startActivity(rateIntent);
    }
}
