package com.example.myapplication

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.*

/**
 *Create by 阿土 on ${date}
 */
class GallerySnapHelper: SnapHelper() {

    private val INVALID_DISTANCE = 1f
    private val MILLISECONDS_PER_INCH = 40f
    private var mHorizontalHelper: OrientationHelper? = null
    private var mRecyclerView: RecyclerView? = null

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        mRecyclerView = recyclerView
        super.attachToRecyclerView(recyclerView)
    }

    /**
     * 该方法会计算第二个参数 对应的itemview当前的坐标到要对的view的坐标的距离
     * 返回int[2] 对应x轴和y轴方向上的距离
     */
    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val array = IntArray(2)
        if (layoutManager.canScrollHorizontally()){
            array[0] = distanceToStart(targetView,getHorizontalHelper(layoutManager))
        }
        return array
    }


    /**
     * 该方法会根据触发fling操作的速率 (参数 velocityX 和velocityY) 确定recyclerview需要滚动到的位置
     * 该位置上的itemview就是要对齐的项  该位置就是TargetSnapPosition  如果找不到就返回RecyclerView.NO_POSITION
     */
    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        //因为layoutmanager很灵活  分为横向/纵向两种布局方式 每个布局上又有方向上的不同 正向/反向
        //计算targetposition时都考虑进去了
        //布局方向就通过RecyclerView.SmoothScroller.ScrollVectorProvider这个接口中的computeScrollVectorForPosition()方法来判断。
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }
        val itemCount = layoutManager.itemCount
        if (itemCount == 0){
            return RecyclerView.NO_POSITION
        }
        val snapView = findSnapView(layoutManager)
        if (null == snapView){
            return RecyclerView.NO_POSITION
        }

        val snapPosition = layoutManager.getPosition(snapView)
        if (snapPosition == RecyclerView.NO_POSITION){
            return RecyclerView.NO_POSITION
        }
        val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider
        val vectorEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
        if (vectorEnd == null){
            return RecyclerView.NO_POSITION
        }

        //手指松开屏幕 列表最多滑动一屏的item数
        val deltaThreshold = layoutManager.width / getHorizontalHelper(layoutManager).getDecoratedMeasurement(snapView)
        var deltaJump = 0
        if (layoutManager.canScrollHorizontally()){
            deltaJump = estimateNextPositionDiffForFling(layoutManager,getHorizontalHelper(layoutManager),velocityX,0)
            if (deltaJump > deltaThreshold){
                deltaJump = deltaThreshold
            }
            if (deltaJump < -deltaThreshold){
                deltaJump = -deltaThreshold
            }
            if (vectorEnd.x < 0){
                deltaJump = -deltaThreshold
            }
        }
        else{
            deltaJump = 0
        }
        if (deltaJump == 0){
            return RecyclerView.NO_POSITION
        }
        var targetPostion = deltaJump + snapPosition
        if (targetPostion < 0){
            targetPostion = 0
        }
        if (targetPostion >= itemCount){
            targetPostion = itemCount - 1
        }
        return targetPostion
    }

    /**
     * 估算位置偏移量
     */
    private fun estimateNextPositionDiffForFling(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val distances = calculateScrollDistance(velocityX, velocityY)
        val perChild = computeDistancePerChild(layoutManager, helper)
        if (perChild <= 0){
            return 0
        }
        val distance = distances[0]
        if (distance > 0){
            return Math.floor((distance / perChild).toDouble()).toInt()
        }
        else{
            return Math.ceil((distance / perChild).toDouble()).toInt()
        }
    }

    /**
     * 计算每个itemview的长度
     */
    private fun computeDistancePerChild(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): Float {
        var minPosView: View? = null
        var maxPosView: View? = null
        var minPos = Int.MIN_VALUE
        var maxPos = Int.MAX_VALUE
        val childCount = layoutManager.childCount
        if (childCount == 0){
            return INVALID_DISTANCE
        }
        for(index in 0 until childCount){
            val view = layoutManager.getChildAt(index)!!
            val position = layoutManager.getPosition(view)
            if (position == RecyclerView.NO_POSITION){
                continue
            }
            if (position < minPos){
                minPos = position
                minPosView = view
            }
            if (position > maxPos){
                maxPos = position
                maxPosView = view
            }
        }

        if (minPosView == null || maxPosView == null){
            return INVALID_DISTANCE
        }
        //因为不确定起点和终点  所以取两个位置的最小值作为起点 最大值作为终点
        val start = Math.min(helper.getDecoratedStart(minPosView), helper.getDecoratedStart(maxPosView))
        val end = Math.max(helper.getDecoratedEnd(minPosView), helper.getDecoratedEnd(maxPosView))
        val distance = end - start
        if (distance == 0){
            return INVALID_DISTANCE
        }
        // 总长度 / 总个数 = 每个item的长度
        return 1f * distance / ((maxPos - minPos) + 1)
    }

    /**
     * 找到当前layoutmanager上最接近对齐位置的那个view  就是snapview
     * 如果返回null 就表示没有要对齐的view 不做滚动对齐处理
     */
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return findStartView(layoutManager,getHorizontalHelper(layoutManager))
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager){
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            if (firstVisibleItemPosition == RecyclerView.NO_POSITION){
                return null
            }
            val itemCount = layoutManager.itemCount
            if (itemCount == 0)
                return null
            val lastCompletelyVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
            //如果是最后一个 那就不做左对齐处理 不然的话会显示不全  返回null 表示不做对齐处理
            if (lastCompletelyVisibleItemPosition == itemCount - 1){
                return null
            }
            val firstView = layoutManager.findViewByPosition(firstVisibleItemPosition)
            //如果第一个itemview被遮住的长度没有超过一半  就把itemview当成snapview
            //否则把下一个itemview当成 snapview
            if (helper.getDecoratedEnd(firstView) >= helper.getDecoratedMeasurement(firstView) / 2
                && helper.getDecoratedEnd(firstView) > 0){
                return firstView
            }
            else{
                return layoutManager.findViewByPosition(firstVisibleItemPosition + 1)
            }
        }
        else{
            return null
        }
    }

    //左对齐
    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        return mHorizontalHelper?: OrientationHelper.createHorizontalHelper(layoutManager)
    }

    /**
     * 该方法是为了设置
     */
    override fun createScroller(layoutManager: RecyclerView.LayoutManager?): RecyclerView.SmoothScroller? {
        return if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView?.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: RecyclerView.SmoothScroller.Action
            ) {
                if (mRecyclerView == null) {
                    return
                }
                val snapDistances = calculateDistanceToFinalSnap(mRecyclerView!!.layoutManager!!, targetView)
                val dx = snapDistances!![0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }
    }
}