package com.xtremelabs.robolectric.shadows;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Constructor;

import static com.xtremelabs.robolectric.Robolectric.getShadowApplication;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlertDialog.class)
public class ShadowAlertDialog extends ShadowDialog {
    @RealObject
    private AlertDialog realAlertDialog;

    private CharSequence[] items;
    private String message;
    private DialogInterface.OnClickListener clickListener;
    private boolean isMultiItem;
    private boolean isSingleItem;
    private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
    private boolean[] checkedItems;
    private int checkedItemIndex;
    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;
    private View view;

    /**
     * Non-Android accessor.
     *
     * @return the most recently created {@code AlertDialog}, or null if none has been created during this test run
     */
    public static AlertDialog getLatestAlertDialog() {
        ShadowAlertDialog dialog = Robolectric.getShadowApplication().getLatestAlertDialog();
        return dialog == null ? null : dialog.realAlertDialog;
    }

    @Override
    @Implementation
    public View findViewById(int viewId) {
        if(view == null) {
            return super.findViewById(viewId);
        }

        return view.findViewById(viewId);
    }

    @Implementation
    public void setView(View view) {
        this.view = view;
    }

    /**
     * Resets the tracking of the most recently created {@code AlertDialog}
     */
    public static void reset() {
        getShadowApplication().setLatestAlertDialog(null);
    }

    /**
     * Simulates a click on the {@code Dialog} item indicated by {@code index}. Handles both multi- and single-choice dialogs, tracks which items are currently
     * checked and calls listeners appropriately.
     *
     * @param index the index of the item to click on
     */
    public void clickOnItem(int index) {
        if (isMultiItem) {
            checkedItems[index] = !checkedItems[index];
            multiChoiceClickListener.onClick(realAlertDialog, index, checkedItems[index]);
        } else {
            if (isSingleItem) {
                checkedItemIndex = index;
            }
            clickListener.onClick(realAlertDialog, index);
        }
    }

    @Implementation
    public Button getButton(int whichButton) {
        switch (whichButton) {
            case AlertDialog.BUTTON_POSITIVE:
                return positiveButton;
            case AlertDialog.BUTTON_NEGATIVE:
                return negativeButton;
            case AlertDialog.BUTTON_NEUTRAL:
                return neutralButton;
        }
        throw new RuntimeException("Only positive, negative, or neutral button choices are recognized");
    }

    /**
     * Non-Android accessor.
     *
     * @return the items that are available to be clicked on
     */
    public CharSequence[] getItems() {
        return items;
    }

    /**
     * Non-Android accessor.
     *
     * @return the message displayed in the dialog
     */
    public String getMessage() {
        return message;
    }

    @Implementation
    public void setMessage(CharSequence message) {
        this.message = (message == null ? null : message.toString());
    }

    /**
     * Non-Android accessor.
     *
     * @return an array indicating which items are and are not clicked on a multi-choice dialog
     */
    public boolean[] getCheckedItems() {
        return checkedItems;
    }

    /**
     * Non-Android accessor.
     *
     * @return return the index of the checked item clicked on a single-choice dialog
     */
    public int getCheckedItemIndex() {
        return checkedItemIndex;
    }

    @Implementation
    public void show() {
        super.show();
        getShadowApplication().setLatestAlertDialog(this);
    }

    /**
     * Shadows the {@code android.app.AlertDialog.Builder} class.
     */
    @Implements(AlertDialog.Builder.class)
    public static class ShadowBuilder {
        @RealObject
        private AlertDialog.Builder realBuilder;

        private CharSequence[] items;
        private DialogInterface.OnClickListener clickListener;
        private DialogInterface.OnCancelListener cancelListener;
        private String title;
        private String message;
        private Context context;
        private boolean isMultiItem;
        private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
        private boolean[] checkedItems;
        private CharSequence positiveText;
        private DialogInterface.OnClickListener positiveListener;
        private CharSequence negativeText;
        private DialogInterface.OnClickListener negativeListener;
        private CharSequence neutralText;
        private DialogInterface.OnClickListener neutralListener;
        private boolean isCancelable;
        private boolean isSingleItem;
        private int checkedItem;

        /**
         * just stashes the context for later use
         *
         * @param context the context
         */
        public void __constructor__(Context context) {
            this.context = context;
        }

        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of the selected item via the supplied listener. This should be
         * an array type i.e. R.array.foo
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        @Implementation
        public AlertDialog.Builder setItems(int itemsId, final DialogInterface.OnClickListener listener) {
            this.isMultiItem = false;

            this.items = context.getResources().getTextArray(itemsId);
            this.clickListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setItems(CharSequence[] items, final DialogInterface.OnClickListener listener) {
            this.isMultiItem = false;

            this.items = items;
            this.clickListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, final DialogInterface.OnClickListener listener) {
            this.isSingleItem = true;
            this.checkedItem = checkedItem;
            this.items = items;
            this.clickListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, final DialogInterface.OnMultiChoiceClickListener listener) {
            this.isMultiItem = true;

            this.items = items;
            this.multiChoiceClickListener = listener;

            if (checkedItems == null) {
                checkedItems = new boolean[items.length];
            } else if (checkedItems.length != items.length) {
                throw new IllegalArgumentException("checkedItems must be the same length as items, or pass null to specify no checked items");
            }
            this.checkedItems = checkedItems;

            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setTitle(CharSequence title) {
            this.title = title.toString();
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setTitle(int titleId) {
            return setTitle(context.getResources().getString(titleId));
        }

        @Implementation
        public AlertDialog.Builder setMessage(CharSequence message) {
            this.message = message.toString();
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setMessage(int messageId) {
            setMessage(context.getResources().getString(messageId));
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setIcon(int iconId) {
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setPositiveButton(CharSequence text, final DialogInterface.OnClickListener listener) {
            this.positiveText = text;
            this.positiveListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setPositiveButton(int positiveTextId, final DialogInterface.OnClickListener listener) {
            return setPositiveButton(context.getResources().getText(positiveTextId), listener);
        }

        @Implementation
        public AlertDialog.Builder setNegativeButton(CharSequence text, final DialogInterface.OnClickListener listener) {
            this.negativeText = text;
            this.negativeListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setNegativeButton(int negativeTextId, final DialogInterface.OnClickListener listener) {
            return setNegativeButton(context.getResources().getString(negativeTextId), listener);
        }

        @Implementation
        public AlertDialog.Builder setNeutralButton(CharSequence text, final DialogInterface.OnClickListener listener) {
            this.neutralText = text;
            this.neutralListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setNeutralButton(int neutralTextId, final DialogInterface.OnClickListener listener) {
            return setNegativeButton(context.getResources().getText(neutralTextId), listener);
        }


        @Implementation
        public AlertDialog.Builder setCancelable(boolean cancelable) {
            this.isCancelable = cancelable;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setOnCancelListener(DialogInterface.OnCancelListener listener) {
            this.cancelListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog create() {
            AlertDialog realDialog;
            try {
                Constructor<AlertDialog> c = AlertDialog.class.getDeclaredConstructor(Context.class);
                c.setAccessible(true);
                realDialog = c.newInstance((Context) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ShadowAlertDialog latestAlertDialog = shadowOf(realDialog);
            latestAlertDialog.context = context;
            latestAlertDialog.items = items;
            latestAlertDialog.setTitle(title);
            latestAlertDialog.message = message;
            latestAlertDialog.clickListener = clickListener;
            latestAlertDialog.setOnCancelListener(cancelListener);
            latestAlertDialog.isMultiItem = isMultiItem;
            latestAlertDialog.isSingleItem = isSingleItem;
            latestAlertDialog.checkedItemIndex = checkedItem;
            latestAlertDialog.multiChoiceClickListener = multiChoiceClickListener;
            latestAlertDialog.checkedItems = checkedItems;
            latestAlertDialog.positiveButton = createButton(realDialog, AlertDialog.BUTTON_POSITIVE, positiveText, positiveListener);
            latestAlertDialog.negativeButton = createButton(realDialog, AlertDialog.BUTTON_NEGATIVE, negativeText, negativeListener);
            latestAlertDialog.neutralButton = createButton(realDialog, AlertDialog.BUTTON_NEUTRAL, neutralText, neutralListener);
            latestAlertDialog.setCancelable(isCancelable);

            return realDialog;
        }

        @Implementation
        public AlertDialog show() {
            AlertDialog dialog = realBuilder.create();
            dialog.show();
            return dialog;
        }

        private Button createButton(final DialogInterface dialog, final int which, CharSequence text, final DialogInterface.OnClickListener listener) {
            Button button = new Button(context);
            button.setText(text);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(dialog, which);
                    }
                }
            });
            return button;
        }
    }
}
