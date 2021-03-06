package com.cnpeng.android2.b_work.b04_flow_layout

import android.content.Intent
import android.graphics.Paint
import android.graphics.Point
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cnpeng.android2.R
import com.cnpeng.android2.d_mine.a01_chips.ChipActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_flow_impl.*
import org.jetbrains.anko.toast

/**
 * CnPeng 2018/12/6 5:35 PM
 * 功用：流式布局/标签实现方式的总结
 * 说明：
 * 1、流式布局/标签的实现方式大致有：
 *  -- 自定义FlowLayout。链接：https://github.com/hongyangAndroid/FlowLayout
 *  -- ChipGroups。     链接：https://www.jianshu.com/p/d64a75ec7c74
 *  -- RecyclerView+StaggerLayoutManager
 *  -- RecyclerView+FlexLayoutManager   链接：https://mp.weixin.qq.com/s/Mi3cK7xujmEMI_rc51-r4g
 *  -- RecyclerView+GridLayoutManager+Span  链接：https://blog.csdn.net/zhq217217/article/details/80421646
 *
 * 2、该DEMO仅演示StaggerLayoutManager、GridLayoutManager、FlexLayoutManager的实现方式
 */
class FlowImplActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var mStaggerAndGvAdapter: FlowAdapter
    lateinit var mActviity: FlowImplActivity
    lateinit var mDataList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_impl)

        mActviity = this

        initRecyclerView()

        initClickEvent()
    }

    private fun initClickEvent() {
        tv_staggerH.setOnClickListener(this)
        tv_staggerV.setOnClickListener(this)
        tv_flex.setOnClickListener(this)
        tv_grid.setOnClickListener(this)
        tv_chip.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        mDataList = initTestData()

        mStaggerAndGvAdapter = FlowAdapter(mDataList)
        rv_flowImpl.adapter = mStaggerAndGvAdapter

        initGridLayoutManager()
        //        initStaggerLayout(true, RecyclerView.VERTICAL)

        initFlexLayout()
    }


    /**
     * CnPeng 2018/12/6 6:17 PM
     * 功用：模拟数据
     * 说明：
     */
    private fun initTestData(): List<String> {
        val dataList = mutableListOf<String>()
        val originStr = "哈哈哈哈哈哈哈哈哈哈"

        for (i in 1..30) {
            val str = originStr.subSequence(0, if (0 == (i % 10)) {
                1
            } else {
                i % 10
            }).toString()

            dataList.add(str)
        }

        return dataList
    }

    override fun onClick(v: View?) {
        val viewId = v?.id
        when (viewId) {
            R.id.tv_staggerH -> {
                initStaggerLayout(false, RecyclerView.HORIZONTAL)
                toast("水平Stagger")
            }

            R.id.tv_staggerV -> {
                initStaggerLayout(true, RecyclerView.VERTICAL)
                toast("垂直Stagger")
            }

            R.id.tv_flex -> {
                rv_flowImpl.visibility = View.GONE
                rv_flowImpl2.visibility = View.VISIBLE
                toast("Flex")
            }

            R.id.tv_chip -> {
                val intent = Intent(mActviity, ChipActivity::class.java)
                startActivity(intent)
                toast("Chip")
            }

            R.id.tv_grid -> {
                initGridLayoutManager()
                toast("GridLayout")
            }
        }
    }


    private fun initGridLayoutManager() {

        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val screenWidth = point.x
        val gridLayoutManager = GridLayoutManager(mActviity, screenWidth)

        val textPaint = Paint()

        //CnPeng 2018/12/10 9:22 AM 配置字体大小,大小需要与条目xml中配置的一致
        textPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, resources.displayMetrics)

        //CnPeng 2018/12/7 4:46 PM 注意这个接口匿名对象的构建方式，前面加了个 object:
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val spanCount = gridLayoutManager.spanCount;

                //条目的padding和margin值。在 xml 中我们设置了margin 为5dp,padding为10dp
                val itemMarginAndPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics)

                val textWidth = textPaint.measureText(mDataList[position])

                val itemWidth: Int = (itemMarginAndPadding * 2 + textWidth).toInt()

                //如果文字的宽度超过屏幕的宽度，那么我们就设置为屏幕宽度。由于强转为int可能会丢失精度，所以保险起见+1
                return (if (itemWidth > spanCount) spanCount else itemWidth) + 1
            }
        }

        rv_flowImpl.layoutManager = gridLayoutManager
        rv_flowImpl.adapter = mStaggerAndGvAdapter
        mStaggerAndGvAdapter.mIsStaggerVertical = false
    }

    private fun initStaggerLayout(b: Boolean, orientation: Int) {

        rv_flowImpl.adapter = mStaggerAndGvAdapter
        rv_flowImpl.layoutManager = StaggeredGridLayoutManager(4, orientation)

        mStaggerAndGvAdapter.mIsStaggerVertical = b
        rv_flowImpl.visibility = View.VISIBLE
        rv_flowImpl2.visibility = View.GONE
    }

    /**
     * CnPeng 2018/12/7 10:10 AM
     * 功用：初始化flex视图
     * 说明：
     * 之所以使用两个RV，是因为使用一个RV的情况下，从Stagger切换到 Flex时会报下列错误：
     * java.lang.ClassCastException: androidx.recyclerview.widget.RecyclerView$LayoutParams cannot be cast to com.google.android.flexbox.FlexItem
     */
    private fun initFlexLayout() {
        val flexAdapter = FlowAdapter(mDataList)
        rv_flowImpl2.adapter = flexAdapter
        val flexLayoutManager = FlexboxLayoutManager(mActviity, FlexDirection.ROW)
        flexLayoutManager.flexWrap = FlexWrap.WRAP

        //CnPeng 2018/12/10 5:28 PM 条目从右向左排列，不设置时默认从左向右排列
        flexLayoutManager.justifyContent = JustifyContent.FLEX_END

        rv_flowImpl2.layoutManager = flexLayoutManager
        rv_flowImpl2.visibility = View.GONE
    }
}