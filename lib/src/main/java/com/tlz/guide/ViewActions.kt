package com.tlz.guide

import android.graphics.Rect
import android.os.Build
import android.support.annotation.IdRes
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorCompat
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.ImageView
import android.widget.TextView
import com.tlz.guide.shapes.Circle
import com.tlz.guide.shapes.RightRect
import com.tlz.guide.shapes.RoundRect
import com.tlz.guide.shapes.Shape


/**
 *
 * Created by Tomlezen.
 * Date: 2017/6/22.
 * Time: 11:07.
 */
class ViewActions internal constructor(private val guide: Guide, private val view: View, private val fitsSystemWindow: Boolean) {

  internal fun dismiss(){
    guide.dismiss()
  }

  fun on(@IdRes viewId: Int): ViewActions = guide.on(viewId)
  fun on(view: View): ViewActions = guide.on(view)

  fun show(): GuideAction = guide.show()
  fun showWithAnimate(animate: ((ViewPropertyAnimatorCompat) -> ViewPropertyAnimatorCompat) = {
    it.alpha(1f)
        .setDuration(view.resources.getInteger(android.R.integer.config_longAnimTime).toLong())
  }): GuideAction = guide.showWithAnimate(animate)

  fun displayView(view: View): ViewEditor<View> = ViewEditor(view, this)
  fun displayText(): ViewEditor<TextView> = ViewEditor(TextView(view.context), this)
  fun displayImage(): ViewEditor<ImageView> = ViewEditor(ImageView(view.context), this)

  fun <T : Shape> displayCustomShape(shape: T, onPreDraw: (rect: Rect) -> Unit):
      CustomShapeViewEditor<T> = CustomShapeViewEditor(onPreDraw, shape, View(view.context), this)
  fun displayCircle(): ShapeViewEditor<Circle> =
      ShapeViewEditor(Circle(), View(view.context), this)
  fun displayRect(): ShapeViewEditor<RightRect> =
      ShapeViewEditor(RightRect(), View(view.context), this)
  fun displayRoundRect(radius: Float = 0f): ShapeViewEditor<RoundRect> =
      ShapeViewEditor(RoundRect(radius), View(view.context), this)

  internal fun <T: View> addView(view: T, editor: ViewEditor<T>) {
    if (editor.anchorViewId == null) {
      withDefaultAnchorViewOnPreDraw {
        layoutAndAddView(this@ViewActions.view, view, editor)
      }
    } else {
      withDefaultAnchorViewOnPreDraw {
        val anchorView = guide.findViewByIdFromInner(editor.anchorViewId!!)
            ?: throw IllegalArgumentException("not found view by anchor id")
        anchorView.onPreDraw {
          if (anchorView.width > 0) {
            layoutAndAddView(anchorView, view, editor)
            true
          } else {
            false
          }
        }
      }
    }
  }

  private fun <T: View> layoutAndAddView(anchorView: View, view: T, editor: ViewEditor<T>) {
    view.onPreDraw {
      if (view.width > 0) {
        val rect = Rect()
        anchorView.getGlobalVisibleRect(rect)
        val x: Int
        val y: Int
        when (editor.location) {
          Location.START, Location.TOP, Location.START or Location.TOP -> {
            x = rect.left - view.width + editor.offsetX
            y = rect.top - view.height + editor.offsetY
          }
          Location.START_INSIDE, Location.TOP_INSIDE, Location.START_INSIDE or Location.TOP_INSIDE -> {
            x = rect.left + editor.offsetX
            y = rect.top + editor.offsetY
          }
          Location.START or Location.TOP_INSIDE -> {
            x = rect.left - view.width + editor.offsetX
            y = rect.top + editor.offsetY
          }
          Location.START or Location.CENTER_VERTICAL, Location.CENTER_VERTICAL -> {
            x = rect.left - view.width + editor.offsetX
            y = (rect.centerY() - view.height / 2f).toInt() + editor.offsetY
          }
          Location.START_INSIDE or Location.CENTER_VERTICAL -> {
            x = rect.left + editor.offsetX
            y = (rect.centerY() - view.height / 2f).toInt() + editor.offsetY
          }
          Location.START or Location.BOTTOM, Location.BOTTOM -> {
            x = rect.left - view.width + editor.offsetX
            y = rect.bottom + editor.offsetY
          }
          Location.START_INSIDE or Location.BOTTOM -> {
            x = rect.left + editor.offsetX
            y = rect.bottom + editor.offsetY
          }
          Location.START or Location.BOTTOM_INSIDE, Location.BOTTOM_INSIDE -> {
            x = rect.left - view.width + editor.offsetX
            y = rect.bottom - view.height + editor.offsetY
          }
          Location.START_INSIDE or Location.BOTTOM_INSIDE -> {
            x = rect.left + editor.offsetX
            y = rect.bottom - view.height + editor.offsetY
          }
          Location.END, Location.END or Location.TOP_INSIDE -> {
            x = rect.right + editor.offsetX
            y = rect.top + editor.offsetY
          }
          Location.END_INSIDE, Location.END_INSIDE or Location.TOP_INSIDE -> {
            x = rect.right - view.width + editor.offsetX
            y = rect.top + editor.offsetY
          }
          Location.END or Location.TOP -> {
            x = rect.right + editor.offsetX
            y = rect.top - view.height + editor.offsetY
          }
          Location.END_INSIDE or Location.TOP -> {
            x = rect.right - view.width + editor.offsetX
            y = rect.top - view.height + editor.offsetY
          }
          Location.END or Location.BOTTOM -> {
            x = rect.right + editor.offsetX
            y = rect.bottom + editor.offsetY
          }
          Location.END_INSIDE or Location.BOTTOM -> {
            x = rect.right - view.width + editor.offsetX
            y = rect.bottom + editor.offsetY
          }
          Location.END or Location.BOTTOM_INSIDE -> {
            x = rect.right + editor.offsetX
            y = rect.bottom - view.height + editor.offsetY
          }
          Location.END_INSIDE or Location.BOTTOM_INSIDE -> {
            x = rect.right - view.width + editor.offsetX
            y = rect.bottom - view.height + editor.offsetY
          }
          Location.END or Location.CENTER_VERTICAL -> {
            x = rect.right + editor.offsetX
            y = (rect.centerY().toFloat() - view.height / 2f).toInt() + editor.offsetY
          }
          Location.END_INSIDE or Location.CENTER_VERTICAL -> {
            x = rect.right - view.width + editor.offsetX
            y = (rect.centerY().toFloat() - view.height / 2f).toInt() + editor.offsetY
          }
          Location.TOP or Location.CENTER_HORIZONTAL, Location.CENTER_HORIZONTAL -> {
            x = (rect.centerX() - view.width / 2f).toInt() + editor.offsetX
            y = rect.top - view.height + editor.offsetY
          }
          Location.TOP_INSIDE or Location.CENTER_HORIZONTAL -> {
            x = (rect.centerX() - view.width / 2f).toInt() + editor.offsetX
            y = rect.top + editor.offsetY
          }
          Location.BOTTOM or Location.CENTER_HORIZONTAL -> {
            x = (rect.centerX() - view.width / 2f).toInt() + editor.offsetX
            y = rect.bottom + editor.offsetY
          }
          Location.BOTTOM_INSIDE or Location.CENTER_HORIZONTAL -> {
            x = (rect.centerX() - view.width / 2f).toInt() + editor.offsetX
            y = rect.bottom - view.height + editor.offsetY
          }
          else -> {
            x = (rect.centerX() - view.width / 2f).toInt() + editor.offsetY
            y = (rect.centerY() - view.height / 2f).toInt() + editor.offsetY
          }
        }

        val translationY = y - getViewOffset().toFloat()
        if (editor.animated) {
          view.translationY = y.toFloat()
          view.translationX = x.toFloat()
          ViewCompat.animate(view)
              .translationY(translationY)
              .setStartDelay(editor.delay)
              .setDuration(editor.duration)
              .apply {
                interpolator = editor.interpolator
              }
              .start()
        } else {
          view.translationY = translationY
          view.translationX = x.toFloat()
        }
        true
      } else {
        false
      }
    }
    guide.addIntoViews(view)
    guide.container.addView(view, editor.layoutParams)
  }

  internal fun <T: Shape> addView(view: View, editor: ShapeViewEditor<T>) {
    withDefaultAnchorViewOnPreDraw {
      val rect = Rect()
      this@ViewActions.view.getGlobalVisibleRect(rect)
      var isCustom = false
      when (editor.shape) {
        is RightRect -> {
          val x = rect.left + editor.padding
          val y = rect.top - getShapeOffset() + editor.padding
          val width = rect.width() - 2 * editor.padding
          val height = rect.height() - 2 * editor.padding
          editor.shape.applySize(x, y, width, height)
        }
        is Circle -> {
          val cx = rect.centerX()
          val cy = rect.centerY() - statusBarOffset
          val radius = Math.max(rect.width(), rect.height()) / 2f
          editor.shape.applySize(cx.toFloat(), cy.toFloat(), Math.sqrt(radius * radius * 2.0).toFloat() - editor.padding)
        }
        else -> {
          isCustom = true
          (editor as CustomShapeViewEditor).onPreDraw(rect)
        }
      }
      guide.shapeContainer.addShape(editor.shape)
      if (!isCustom) {
        val width = rect.width() * (editor.additionalRatio + 1)
        val height = rect.height() * (editor.additionalRatio + 1)
        val x = rect.left - (width - rect.width()) / 2
        val y = rect.top - (height - rect.height()) / 2 - getShapeOffset()
        view.layoutParams.width = width.toInt()
        view.layoutParams.height = height.toInt()
        view.translationY = y
        view.translationX = x
      }
      guide.addIntoViews(view)
      guide.container.addView(view)
    }
  }

  private fun withDefaultAnchorViewOnPreDraw(block: () -> Unit) {
    view.onPreDraw {
      block()
      true
    }
  }

  private fun View.onPreDraw(onPreDraw: () -> Boolean) {
    viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
      override fun onPreDraw(): Boolean {
        if (onPreDraw()) {
          view.viewTreeObserver.removeOnPreDrawListener(this)
        }
        return false
      }
    })
  }

  private val statusBarOffset: Int
    get() {
      var result = 0
      val context = view.context
      val resources = context.resources
      val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
      if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
      }
      return result
    }

  private fun getViewOffset(): Int {
    if (this.fitsSystemWindow || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      return statusBarOffset
    }
    return 0
  }

  private fun getShapeOffset(): Int {
    if (this.fitsSystemWindow || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      return statusBarOffset
    }
    return 0
  }
}