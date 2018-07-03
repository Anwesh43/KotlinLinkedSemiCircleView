package com.anwesh.uiprojects.linkedsemicircleview

/**
 * Created by anweshmishra on 03/07/18.
 */

import android.graphics.Paint
import android.graphics.Canvas
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Color
import android.graphics.RectF

val SEMI_CIRCLE_NODES = 5
val color : Int = Color.parseColor("#512DA8")

class LinkedSemiCircleView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += 0.1f * dir
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }
    }

    data class SemiCircleNode(var i : Int, val state : State = State()) {

        var next : SemiCircleNode? = null

        var prev : SemiCircleNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < SEMI_CIRCLE_NODES - 1) {
                next = SemiCircleNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            prev?.draw(canvas, paint)
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = 0.8f * w / SEMI_CIRCLE_NODES
            val r : Float = gap / 5
            val index : Int = i % 2
            val deg : Float = (180f) * index
            val rot : Float = 180f * (1 - 2 * index) * state.scales[0]
            paint.strokeWidth = Math.min(w, h) / 50
            paint.strokeCap = Paint.Cap.ROUND
            paint.style = Paint.Style.STROKE
            paint.color = color
            canvas.save()
            canvas.translate(0.1f * w + gap * i + gap * state.scales[1], h/2)
            canvas.rotate(deg + rot)
            canvas.drawArc(RectF(-r, -r, r, r), 0f, 180f, false, paint)
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SemiCircleNode{
            var curr : SemiCircleNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedSemiCircle(var i : Int) {
        private var curr : SemiCircleNode = SemiCircleNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedSemiCircleView) {

        private val lsc : LinkedSemiCircle = LinkedSemiCircle(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lsc.draw(canvas, paint)
            animator.animate {
                lsc.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lsc.startUpdating {
                animator.start()
            }
        }
    }
}