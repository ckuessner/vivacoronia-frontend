package de.tudarmstadt.iptk.foxtrot.vivacoronia.animation

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

fun View.expand() {
    val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec((parent as View).width, View.MeasureSpec.EXACTLY)
    val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight = measuredHeight
    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    layoutParams.height = 1
    visibility = View.VISIBLE

    val animation = object: Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            layoutParams.height = if (interpolatedTime.toInt() == 1) ViewGroup.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    animation.duration = (targetHeight / context.resources.displayMetrics.density).toLong()
    startAnimation(animation)
}

fun View.collapse() {
    val initialHeight = measuredHeight

    val animation = object: Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime.toInt() == 1)
                visibility = View.GONE
            else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    animation.duration = (initialHeight / context.resources.displayMetrics.density).toLong()
    startAnimation(animation)
}