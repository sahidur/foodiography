package me.sahidur.foodiography.main.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.elevation.Elevation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class Config {

    public static String toTheUpperCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static String toTheUpperCaseSingle(String givenString) {
        String example = givenString;

        example = example.substring(0, 1).toUpperCase()
                + example.substring(1, example.length());

        System.out.println(example);
        return example;
    }

    public static double getDensityDpi(Context mContext) {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();

        double density = dm.density * 160;
        double x = Math.pow(dm.widthPixels / density, 2);
        double y = Math.pow(dm.heightPixels / density, 2);

        return Math.sqrt(x + y);
    }

    public static int dpToPx(Context mContext, int dp) {
        Resources r = mContext.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static float distanceFrom(double lat1, double lng1, double lat2, double lng2) {
        Location markerLoc = new Location("Marker");
        markerLoc.setLatitude(lat2);
        markerLoc.setLongitude(lng2);
        Location currentLoc = new Location("Current");
        currentLoc.setLatitude(lat1);
        currentLoc.setLongitude(lng1);
        return currentLoc.distanceTo(markerLoc);
    }


    public static void makeLinkClickable(final Context mContext, SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...
                Intent baseIntent = new Intent(Intent.ACTION_VIEW);
                Intent chooserIntent = Intent.createChooser(baseIntent, "Select Application");
                baseIntent.setData(Uri.parse(span.getURL()));
                mContext.startActivity(chooserIntent);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public static void setTextViewHTML(Context mContext, TextView text, String html)
    {
        CharSequence sequence;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            sequence = Html.fromHtml(html);
        }
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(mContext, strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }


    public static double getElevation(double lat, double lng) {
        String loc = lat +","+lng;
        final double[] elev = new double[1];
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.ELEV).build();
        final RestInterface restInterface = adapter.create(RestInterface.class);
        restInterface.getElevation(loc, RestInterface.sensor, new Callback<Elevation>() {
            @Override
            public void success(Elevation model, Response response) {
                if (model.getStatus().equalsIgnoreCase("ok")) {
                    elev[0] = model.getResults().get(0).getElevation();
                    Log.w("Get Address", ":"+model.getResults().get(0).getElevation());
                }else{
                    elev[0] = 0.0;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("ERROR GET ELEV", ":"+error.getMessage());
                elev[0] = 0.0;
            }
        });
        return elev[0];
    }

    public static String cDist(float dist){
        String d = String.valueOf(String.format("%.2f", dist / 1000f)) + " Km";
        return d;
    }

    public static void showSettingsAlert(final Context mContext) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(R.string.gps_settings);
        alertDialog.setMessage(R.string.gps_failed);
        alertDialog.setPositiveButton(R.string.setting_test, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public static void alertLoginAnon(Context mContext, String messg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext, R.style.CustomDialog);
        alertDialog.setMessage(messg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static boolean checkConnection(Context cek) {
        ConnectivityManager cm = (ConnectivityManager) cek.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static void showToast(View v, String text) {
        Snackbar snackbar = Snackbar
                .make(v, text, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static Date serverDate(String timeStamp){
        Date OurDate = null;

        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(timeStamp);;

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getDefault());
            String n = dateFormatter.format(value);
            OurDate = dateFormatter.parse(n);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return OurDate;
    }

    public static Date getDefault(){
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        f.setTimeZone(TimeZone.getDefault());
        Date nowDate = new Date();
        try {
            nowDate = dateFormatter.parse(f.format(new Date()));
        }catch (ParseException e){
            e.printStackTrace();
        }
        return nowDate;
    }
}
