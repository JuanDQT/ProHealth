package com.juan.prohealth.custom

// https://www.vogella.com/tutorials/AndroidCustomViews/article.html
import android.app.Service
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.juan.prohealth.R
import com.juan.prohealth.alignTo
import kotlinx.android.synthetic.main.custom_button.view.*

class CustomButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    init {

        val inflater = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.custom_button, this)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.custom_button_attrs, 0, 0)
            val title = resources.getText(typedArray .getResourceId(R.styleable.custom_button_attrs_cb_title, R.string.empty))
            my_textview.text = title
            my_iv.setBackgroundResource(typedArray .getResourceId(R.styleable.custom_button_attrs_cb_resource, R.string.empty))

            if (typedArray.hasValue(R.styleable.custom_button_attrs_cb_icon_position)) {
                when (typedArray.getString(R.styleable.custom_button_attrs_cb_icon_position)) {
                    "0" -> {
                        return@let
                    }
                    "1" -> {
                        my_cv.alignTo(RelativeLayout.ALIGN_PARENT_END)
                        my_textview.alignTo(RelativeLayout.ALIGN_PARENT_START)
                        my_textview.alignTo(RelativeLayout.ALIGN_PARENT_END)
                        val lpCIV = my_textview.layoutParams as RelativeLayout.LayoutParams
                        lpCIV.leftMargin = 0
                        lpCIV.rightMargin = 40
                        my_textview.layoutParams = lpCIV
                    }
                }
            }
            typedArray.recycle()
        }
    }
}

