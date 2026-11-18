import android.content.Intent;
import android.content.SharedPreferences;
package ir.personal.smartfinance;
package ir.personal.smartfinance;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    LinearLayout root, content;
    EditText titleInput, amountInput, noteInput;
    Spinner typeSpinner;
    SharedPreferences prefs;
    JSONArray entries;

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView text(String value, int size) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(size);
        textView.setTextColor(Color.rgb(35, 45, 60));
        textView.setPadding(dp(10), dp(8), dp(10), dp(8));
        return textView;
    }

    Button button(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setAllCaps(false);
        return button;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        prefs = getSharedPreferences("finance", MODE_PRIVATE);

        try {
            String saved = prefs.getString("entries", "[]");
            entries = new JSONArray(saved);
        } catch (Exception ignored) {
            entries = new JSONArray();
        }

        showHome();
    }

    void base(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));
        root.setBackgroundColor(Color.rgb(246, 248, 251));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView titleView = text(title, 20);
        titleView.setTextColor(Color.rgb(25, 35, 50));
        root.addView(titleView);

        ScrollView scrollView = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView.addView(content);

        root.addView(scrollView,
                new LinearLayout.LayoutParams(-1, -1));

        setContentView(root);
    }

    void addButton(String label, Runnable action) {
        Button button = button(label);
        content.addView(button);
        button.setOnClickListener(view -> action.run());
    }

    void save() {
        prefs.edit()
                .putString("entries", entries.toString())
                .apply();
    }

    long number(String value) {
        try {
            String digits = value
                    .replace(",", "")
                    .replace("٬", "")
                    .replace("٫", "")
                    .replace("۰", "0")
                    .replace("۱", "1")
                    .replace("۲", "2")
                    .replace("۳", "3")
                    .replace("۴", "4")
                    .replace("۵", "5")
                    .replace("۶", "6")
                    .replace("۷", "7")
                    .replace("۸", "8")
                    .replace("۹", "9")
                    .trim();

            if (digits.isEmpty()) return 0;
            return Long.parseLong(digits);
        } catch (Exception ignored) {
            return 0;
        }
    }

    String money(long amount) {
        return String.format(
                Locale.US,
                "%,d",
                amount
        ) + " تومان";
    }

    long sum(String type) {
        long result = 0;

        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.optJSONObject(i);

            if (item != null &&
                    type.equals(item.optString("type"))) {
                result += item.optLong("amount");
            }
        }

        return result;
    }

    void showHome() {
        base("حسابداری شخصی تانریا");

        content.addView(text(
                "داشبورد مالی • " +
                        new SimpleDateFormat(
                                "yyyy/MM/dd HH:mm",
                                Locale.getDefault()
                        ).format(new Date()),
                14
        ));

        long income = sum("درآمد");
        long expense = sum("هزینه");

        content.addView(text("مجموع درآمد: " + money(income), 18));
        content.addView(text("مجموع هزینه: " + money(expense), 18));
        content.addView(text(
                "ماندهٔ ثبت‌شده: " + money(income - expense),
                20
        ));

        addButton(
                "＋ ثبت درآمد یا هزینه",
                this::showAdd
        );

        addButton(
                "📒 دفتر تراکنش‌ها",
                this::showEntries
        );

        addButton(
                "📊 نمودار کلی",
                this::showChart
        );

        addButton(
                "📅 گزارش بر اساس بازهٔ تاریخ",
                this::showReportDialog
        );

        addButton(
                "🖼 انتخاب عکس فاکتور / رسید",
                this::pickImage
        );

        addButton(
                "🤖 دستیار مالی هوشمند",
                () -> new AlertDialog.Builder(this)
                        .setTitle("دستیار مالی")
                        .setMessage("این نسخه هنوز به مدل هوش مصنوعی متصل نیست.")
                        .setPositiveButton("متوجه شدم", null)
                        .show()
        );
    }

    void showAdd() {
        base("ثبت تراکنش جدید");

        typeSpinner = new Spinner(this);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        new String[]{"هزینه", "درآمد"}
                );

        typeSpinner.setAdapter(adapter);

        content.addView(text("نوع تراکنش", 16));
        content.addView(typeSpinner);

        titleInput = new EditText(this);
        titleInput.setSingleLine();
        titleInput.setHint("عنوان (مثلاً خرید تجهیزات)");
        content.addView(titleInput);

        amountInput = new EditText(this);
        amountInput.setSingleLine();
        amountInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );
        amountInput.setHint("مبلغ به تومان");
        content.addView(amountInput);

        // قالب‌بندی خودکار سه‌رقمی
        amountInput.addTextChangedListener(new TextWatcher() {
            boolean editing;

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editing) return;
                editing = true;

                String value = editable.toString()
                        .replace(",", "")
                        .replace("٬", "")
                        .replace(" ", "")
                        .replace("۰", "0")
                        .replace("۱", "1")
                        .replace("۲", "2")
                        .replace("۳", "3")
                        .replace("۴", "4")
                        .replace("۵", "5")
                        .replace("۶", "6")
                        .replace("۷", "7")
                        .replace("۸", "8")
                        .replace("۹", "9");

                if (value.isEmpty()) {
                    editable.clear();
                    editing = false;
                    return;
                }

                try {
                    long amount = Long.parseLong(value);
                    String formatted = String.format(
                            Locale.US,
                            "%,d",
                            amount
                    );

                    editable.replace(
                            0,
                            editable.length(),
                            formatted
                    );
                    amountInput.setSelection(
                            amountInput.getText().length()
                    );
                } catch (Exception ignored) {
                }

                editing = false;
            }
        });

        noteInput = new EditText(this);
        noteInput.setHint("توضیحات (اختیاری)");
        content.addView(noteInput);

        addButton("ذخیره تراکنش", () -> {
            String title = titleInput.getText()
                    .toString()
                    .trim();

            long amount = number(
                    amountInput.getText().toString()
            );

            if (title.isEmpty() || amount <= 0) {
                Toast.makeText(
                        this,
                        "عنوان و مبلغ معتبر وارد کن",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            JSONObject item = new JSONObject();

            try {
                item.put("type",
                        typeSpinner.getSelectedItem().toString());
                item.put("title", title);
                item.put("amount", amount);
                item.put("note",
                        noteInput.getText().toString());
                item.put("date",
                        new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.US
                        ).format(new Date()));

                entries.put(item);
                save();

                Toast.makeText(
                        this,
                        "ذخیره شد",
                        Toast.LENGTH_SHORT
                ).show();

                showHome();
            } catch (Exception ignored) {
                Toast.makeText(
                        this,
                        "ذخیره انجام نشد",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        addButton("بازگشت", this::showHome);
    }

    void showEntries() {
        base("دفتر تراکنش‌ها");

        if (entries.length() == 0) {
            content.addView(
                    text("هنوز تراکنشی ثبت نشده است.", 16)
            );
        }

        for (int i = entries.length() - 1; i >= 0; i--) {
            JSONObject item = entries.optJSONObject(i);

            if (item == null) continue;

            String note = item.optString("note");

            content.addView(text(
                    item.optString("type") +
                            " — " +
                            item.optString("title") +
                            "\n" +
                            money(item.optLong("amount")) +
                            "\n" +
                            persianDateText(
                                    item.optString("date")
                            ) +
                            (note.isEmpty() ? "" : "\n" + note),
                    16
            ));
        }

        addButton("بازگشت", this::showHome);
    }

    void showChart() {
        base("نمودار درآمد و هزینه");

        long income = sum("درآمد");
        long expense = sum("هزینه");

        content.addView(text(
                "نمودار مقایسه‌ای بر اساس تمام تراکنش‌ها",
                14
        ));

        content.addView(
                new View(this) {
                    final Paint paint = new Paint();

                    @Override
                    protected void onDraw(Canvas canvas) {
                        super.onDraw(canvas);

                        float width = getWidth();
                        float height = getHeight();

                        paint.setColor(Color.LTGRAY);
                        canvas.drawRect(
                                0,
                                height * .15f,
                                width,
                                height * .85f,
                                paint
                        );

                        float maximum = Math.max(
                                1,
                                Math.max(income, expense)
                        );

                        paint.setColor(Color.rgb(35, 145, 95));
                        canvas.drawRect(
                                width * .18f,
                                height * .85f,
                                width * .38f,
                                height * .85f -
                                        height * .65f * income / maximum,
                                paint
                        );

                        paint.setColor(Color.rgb(220, 90, 80));
                        canvas.drawRect(
                                width * .58f,
                                height * .85f,
                                width * .78f,
                                height * .85f -
                                        height * .65f * expense / maximum,
                                paint
                        );

                        paint.setColor(Color.DKGRAY);
                        paint.setTextSize(dp(13));
                        canvas.drawText(
                                "درآمد",
                                width * .18f,
                                height * .97f,
                                paint
                        );
                        canvas.drawText(
                                "هزینه",
                                width * .58f,
                                height * .97f,
                                paint
                        );
                    }
                },
                new LinearLayout.LayoutParams(-1, dp(220))
        );

        content.addView(text(
                "درآمد: " + money(income) +
                        "\nهزینه: " + money(expense),
                16
        ));

        addButton("بازگشت", this::showHome);
    }

    /* ================= گزارش بازهٔ شمسی ================= */

    void showReportDialog() {
        Calendar today = Calendar.getInstance();

        int[] todayPersian =
                gregorianToJalali(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH) + 1,
                        today.get(Calendar.DAY_OF_MONTH)
                );

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(10), dp(20), dp(10));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        EditText fromYear = new EditText(this);
        fromYear.setHint("از سال — مثل ۱۴۰۴");
        fromYear.setText(String.valueOf(todayPersian[0]));

        EditText fromMonth = new EditText(this);
        fromMonth.setHint("از ماه — عدد ۱ تا ۱۲");
        fromMonth.setText("1");

        EditText fromDay = new EditText(this);
        fromDay.setHint("از روز");
        fromDay.setText("1");

        EditText toYear = new EditText(this);
        toYear.setHint("تا سال");
        toYear.setText(String.valueOf(todayPersian[0]));

        EditText toMonth = new EditText(this);
        toMonth.setHint("تا ماه");
        toMonth.setText(String.valueOf(todayPersian[1]));

        EditText toDay = new EditText(this);
        toDay.setHint("تا روز");
        toDay.setText(String.valueOf(todayPersian[2]));

        layout.addView(text("بازهٔ شروع", 16));
        layout.addView(fromYear);
        layout.addView(fromMonth);
        layout.addView(fromDay);

        layout.addView(text("بازهٔ پایان", 16));
        layout.addView(toYear);
        layout.addView(toMonth);
        layout.addView(toDay);

        new AlertDialog.Builder(this)
                .setTitle("گزارش بازهٔ زمانی شمسی")
                .setView(layout)
                .setPositiveButton("نمایش گزارش", (dialog, which) -> {
                    try {
                        Calendar start = jalaliToGregorian(
                                persianNumber(fromYear),
                                persianNumber(fromMonth),
                                persianNumber(fromDay)
                        );

                        Calendar end = jalaliToGregorian(
                                persianNumber(toYear),
                                persianNumber(toMonth),
                                persianNumber(toDay)
                        );

                        start.set(Calendar.HOUR_OF_DAY, 0);
                        start.set(Calendar.MINUTE, 0);
                        start.set(Calendar.SECOND, 0);
                        start.set(Calendar.MILLISECOND, 0);

                        end.set(Calendar.HOUR_OF_DAY, 23);
                        end.set(Calendar.MINUTE, 59);
                        end.set(Calendar.SECOND, 59);
                        end.set(Calendar.MILLISECOND, 999);

                        if (start.after(end)) {
                            Toast.makeText(
                                    this,
                                    "تاریخ شروع نباید بعد از پایان باشد",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        showReport(start, end);
                    } catch (Exception ignored) {
                        Toast.makeText(
                                this,
                                "تاریخ‌ها را به‌درستی وارد کنید",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    int persianNumber(EditText editText) {
        String text = editText.getText()
                .toString()
                .trim();

        text = text
                .replace("۰", "0")
                .replace("۱", "1")
                .replace("۲", "2")
                .replace("۳", "3")
                .replace("۴", "4")
                .replace("۵", "5")
                .replace("۶", "6")
                .replace("۷", "7")
                .replace("۸", "8")
                .replace("۹", "9");

        return Integer.parseInt(text);
    }

    void showReport(Calendar start, Calendar end) {
        base("گزارش بازهٔ زمانی");

        int[] startPersian = gregorianToJalali(
                start.get(Calendar.YEAR),
                start.get(Calendar.MONTH) + 1,
                start.get(Calendar.DAY_OF_MONTH)
        );

        int[] endPersian = gregorianToJalali(
                end.get(Calendar.YEAR),
                end.get(Calendar.MONTH) + 1,
                end.get(Calendar.DAY_OF_MONTH)
        );

        content.addView(text(
                "از: " + startPersian[0] + "/" +
                        startPersian[1] + "/" + startPersian[2] +
                        "\nتا: " + endPersian[0] + "/" +
                        endPersian[1] + "/" + endPersian[2],
                16
        ));

        long income = 0;
        long expense = 0;
        int transactionCount = 0;

        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.optJSONObject(i);

            if (item == null) continue;

            Calendar transactionDate =
                    parseTransactionDate(item.optString("date"));

            if (transactionDate == null) continue;

            if (transactionDate.before(start) ||
                    transactionDate.after(end)) {
                continue;
            }

            transactionCount++;

            long amount = item.optLong("amount");

            if ("درآمد".equals(item.optString("type"))) {
                income += amount;
            } else if ("هزینه".equals(item.optString("type"))) {
                expense += amount;
            }
        }

        content.addView(text(
                "تعداد تراکنش‌ها: " + transactionCount,
                16
        ));

        content.addView(text(
                "درآمد بازه: " + money(income),
                18
        ));

        content.addView(text(
                "هزینه بازه: " + money(expense),
                18
        ));

        content.addView(text(
                "مانده بازه: " + money(income - expense),
                20
        ));

        content.addView(text("تراکنش‌های بازه:", 16));

        boolean found = false;

        for (int i = entries.length() - 1; i >= 0; i--) {
            JSONObject item = entries.optJSONObject(i);

            if (item == null) continue;

            Calendar date =
                    parseTransactionDate(item.optString("date"));

            if (date == null) continue;

            if (date.before(start) || date.after(end)) continue;

            found = true;

            content.addView(text(
                    item.optString("type") +
                            " — " +
                            item.optString("title") +
                            "\n" +
                            money(item.optLong("amount")) +
                            "\n" +
                            persianDateText(
                                    item.optString("date")
                            ),
                    15
            ));
        }

        if (!found) {
            content.addView(text(
                    "تراکنشی در این بازه یافت نشد.",
                    16
            ));
        }

        addButton("بازگشت", this::showHome);
    }

    Calendar parseTransactionDate(String dateText) {
        try {
            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.US
                    );

            format.setLenient(false);

            Date date = format.parse(dateText);

            if (date == null) return null;

            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(date);
            return calendar;
        } catch (ParseException ignored) {
            return null;
        }
    }

    String persianDateText(String gregorianDate) {
        Calendar calendar =
                parseTransactionDate(gregorianDate);

        if (calendar == null) {
            return gregorianDate;
        }

        int[] jalali = gregorianToJalali(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        return jalali[0] + "/" +
                jalali[1] + "/" +
                jalali[2];
    }

    /* ================= تبدیل تاریخ شمسی ================= */

    boolean isJalaliLeap(int year) {
        int[] leapYears = {1, 5, 9, 13, 17, 22, 26, 30};

        int remainder = ((year % 33) + 33) % 33;

        for (int leap : leapYears) {
            if (remainder == leap) return true;
        }

        return false;
    }

    int jalaliYearLength(int year) {
        return isJalaliLeap(year) ? 366 : 365;
    }

    int jalaliMonthLength(int year, int month) {
        if (month >= 1 && month <= 6) return 31;
        if (month >= 7 && month <= 11) return 30;
        return isJalaliLeap(year) ? 30 : 29;
    }

    Calendar jalaliToGregorian(
            int year,
            int month,
            int day
    ) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException();
        }

        if (day < 1 ||
                day > jalaliMonthLength(year, month)) {
            throw new IllegalArgumentException();
        }

        Calendar anchor =
                Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        anchor.clear();
        anchor.set(622, Calendar.MARCH, 22);
        anchor.set(Calendar.HOUR_OF_DAY, 0);
        anchor.set(Calendar.MINUTE, 0);
        anchor.set(Calendar.SECOND, 0);
        anchor.set(Calendar.MILLISECOND, 0);

        for (int y = 1; y < year; y++) {
            anchor.add(
                    Calendar.DAY_OF_MONTH,
                    jalaliYearLength(y)
            );
        }

        for (int m = 1; m < month; m++) {
            anchor.add(
                    Calendar.DAY_OF_MONTH,
                    jalaliMonthLength(year, m)
            );
        }

        anchor.add(
                Calendar.DAY_OF_MONTH,
                day - 1
        );

        return anchor;
    }

    int[] gregorianToJalali(
            int gregorianYear,
            int gregorianMonth,
            int gregorianDay
    ) {
        Calendar anchor =
                Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        anchor.clear();
        anchor.set(622, Calendar.MARCH, 22);
        anchor.set(Calendar.HOUR_OF_DAY, 0);
        anchor.set(Calendar.MINUTE, 0);
        anchor.set(Calendar.SECOND, 0);
        anchor.set(Calendar.MILLISECOND, 0);

        Calendar current =
                Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        current.clear();
        current.set(
                gregorianYear,
                gregorianMonth - 1,
                gregorianDay
        );
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);

        long difference = current.getTimeInMillis()
                - anchor.getTimeInMillis();

        int remainingDays =
                (int) TimeUnit.MILLISECONDS.toDays(difference);

        if (remainingDays < 0) {
            return new int[]{1300, 1, 1};
        }

        int year = 1;

        while (remainingDays >=
                jalaliYearLength(year)) {
            remainingDays -=
                    jalaliYearLength(year);
            year++;
        }

        int month = 1;

        while (month <= 12) {
            int length =
                    jalaliMonthLength(year, month);

            if (remainingDays < length) break;

            remainingDays -= length;
            month++;
        }

        int day = remainingDays + 1;

        return new int[]{year, month, day};
    }

    void pickImage() {
        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(intent, 101);
        } catch (Exception ignored) {
            Toast.makeText(
                    this,
                    "انتخاب تصویر در دسترس نیست",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int request,
            int result,
            Intent data
    ) {
        super.onActivityResult(request, result, data);

        if (request == 101 &&
                result == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            Uri uri = data.getData();

            getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

            new AlertDialog.Builder(this)
                    .setTitle("تصویر انتخاب شد")
                    .setMessage(
                            "تصویر انتخاب شد:\n" +
                                    uri.toString()
                    )
                    .setPositiveButton(
                            "باشه",
                            (dialog, which) -> showHome()
                    )
                    .show();
        }
    }
                   }
