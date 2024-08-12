package com.ocean.pdfcreateviewshareapp

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.draw.LineSeparator
import com.ocean.pdfcreateviewshareapp.databinding.ActivityMain2Binding
import com.ocean.pdfcreateviewshareapp.databinding.ActivityMainBinding
import com.ocean.pdfcreateviewshareapp.itextpdf.AddDataItemActionAlternateRowColor
import com.ocean.pdfcreateviewshareapp.itextpdf.AddDataItemActionIn2Column
import com.ocean.pdfcreateviewshareapp.itextpdf.AddImageAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddImagesInRowAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddParagraphAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddTittleTextActionBgColor
import com.ocean.pdfcreateviewshareapp.itextpdf.PdfAction
import com.ocean.pdfcreateviewshareapp.itextpdf.PdfContentProvider
import com.ocean.pdfcreateviewshareapp.itextpdf.PdfUtil

class MainActivity2 : AppCompatActivity(), PdfContentProvider {

    private lateinit var binding : ActivityMain2Binding


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.idBtnGeneratePdf.setOnClickListener {
            PdfUtil.createAndDisplayPdf(this, "pdf", this)
        }
        binding.idBtnSharePdf.setOnClickListener {
            PdfUtil.createAndDisplayPdf(this, "share", this)
        }


    }

    override fun providePdfActions(): List<PdfAction?> {
        val mColorAccent = BaseColor(0, 153, 204, 255)
        val headerFont =
            BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.EMBEDDED)
        val fontBody = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED)
        val lineSeparator = LineSeparator().apply { lineColor = BaseColor(0, 0, 0, 68) }
        val mOrderIdFont = Font(fontBody, 18.0f, Font.NORMAL, BaseColor.BLACK)
        val headerBoldFont = Font(fontBody, 18.0f, Font.BOLD, BaseColor.BLACK)

        return listOf(
            AddImagesInRowAction(this, R.drawable.ic_android_black_24dp, R.drawable.android_whole_icon, 100, 75),
            AddImageAction(this, R.drawable.baseline_bakery_dining_24, 100, 75),
            AddTittleTextActionBgColor(this,"Something-- Receipt", 25.0f, headerFont),
            AddParagraphAction(),
//            AddParagraphAction(),
//            AddLineSeparatorAction(lineSeparator),

            AddDataItemActionIn2Column("Status", "Success",mOrderIdFont),
            AddDataItemActionIn2Column( "Amount", "Rs. 2500.00",mOrderIdFont),
            AddDataItemActionIn2Column("Bank Name", "SBI",mOrderIdFont),
            AddDataItemActionIn2Column("Status2", "Success",mOrderIdFont),
            AddDataItemActionIn2Column("Amount2", "Rs. 2500.00",mOrderIdFont),
            AddDataItemActionIn2Column("Bank Name2", "SBI",mOrderIdFont),
            AddParagraphAction()
        )
    }
}