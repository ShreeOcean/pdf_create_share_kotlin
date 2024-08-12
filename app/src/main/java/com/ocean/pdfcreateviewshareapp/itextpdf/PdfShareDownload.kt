package com.ocean.pdfcreateviewshareapp.itextpdf

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import com.ocean.pdfcreateviewshareapp.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

interface PdfAction {
    fun perform(document: Document)
}

interface PdfContentProvider{
    fun providePdfActions(): List<PdfAction?>
}

class AddTittleTextAction(
    private val text: String,
    private val size: Float,
    private val font: BaseFont,
    private val textStyle: Int = Font.NORMAL,
    private val color: BaseColor = BaseColor.BLACK,
    private val alignment: Int = Element.ALIGN_CENTER
) : PdfAction {
    override fun perform(document: Document) {
        val chunk = Chunk(text, Font(font, size, textStyle, color))
        val paragraph = Paragraph(chunk)
        paragraph.alignment = alignment
        document.add(paragraph)
    }
}

/** This method add required color in the in-build lib BaseColor class */
fun getBaseColorFromResource(context: Context, colorResId: Int):BaseColor{
    val colorInt = ContextCompat.getColor(context, colorResId)
    return BaseColor(
        android.graphics.Color.red(colorInt),
        android.graphics.Color.green(colorInt),
        android.graphics.Color.blue(colorInt)
    )
}

class AddTittleTextActionBgColor(
    private val context: Context,
    private val headerText: String,
    private val size: Float,
    private val font: BaseFont,
    private val textStyle: Int = Font.NORMAL,
    private val textColor: BaseColor = BaseColor.WHITE,
    private val backgroundColorsId : Int = R.color.orange
): PdfAction{
    override fun perform(document: Document) {
        val table = PdfPTable(1)
        table.widthPercentage = 100f
        val fontWithColor = Font(font,size,textStyle,textColor)
        val phrase = Phrase(headerText, fontWithColor)
        val bgColor = getBaseColorFromResource(context, backgroundColorsId)

        val headertext = PdfPCell(phrase).apply {
            border = PdfPCell.NO_BORDER
            horizontalAlignment = Element.ALIGN_LEFT
            this.backgroundColor = bgColor
            paddingBottom = 10f
            verticalAlignment = Element.ALIGN_LEFT
        }
        table.addCell(headertext)
        document.add(table)
    }

}

class AddDataItemActionIn2Column(
    private val valueCol1: String,
    private val valueCol2: String,
    private val dataFont: Font,
): PdfAction {
    override fun perform(document: Document) {
        val table = PdfPTable(4).apply {
            widthPercentage = 100f
            setWidths(floatArrayOf(2f,3f,1f,1f))// adjust width as needed
        }
        //create cells for the first two columns with data
        val valCol1 = PdfPCell(Phrase(valueCol1, dataFont)).apply {
            border = PdfPCell.NO_BORDER
            horizontalAlignment = Element.ALIGN_LEFT
        }
        val valCol2 = PdfPCell(Phrase(valueCol2, dataFont)).apply {
            border = PdfPCell.NO_BORDER
            horizontalAlignment = Element.ALIGN_LEFT
        }
        //create empty cells for the last 2 columns
        val emptyCol3 = PdfPCell(Phrase("")).apply {
            border = PdfPCell.NO_BORDER
        }
        val emptyCol4 = PdfPCell(Phrase("")).apply {
            border = PdfPCell.NO_BORDER
        }

        table.addCell(valCol1)
        table.addCell(valCol2)
        table.addCell(emptyCol3)
        table.addCell(emptyCol4)
        document.add(table)
    }
}

class AddDataItemActionInTable4Col(
    private val label: String,
    private val value: String,
    private val font: Font,
): PdfAction {
    override fun perform(document: Document) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        val labelCell = PdfPCell(Phrase(label, font)).apply {
            border = PdfPCell.ANCHOR
            horizontalAlignment = Element.ALIGN_LEFT
        }
        val valueCell = PdfPCell(Phrase(value, font)).apply {
            border = PdfPCell.ANCHOR
            horizontalAlignment = Element.ALIGN_LEFT
        }
        table.addCell(labelCell)
        table.addCell(valueCell)
        document.add(table)
//        document.add(Paragraph("$label: $value", font))
    }
}

class AddDataItemActionAlternateRowColor(
    private val context: Context,
    private val label: String,
    private val value: String,
    private val font: Font,
    private val rowIndex: Int,
): PdfAction{
    override fun perform(document: Document) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        val greyColor = getBaseColorFromResource(context, R.color.light_grey)
        val whiteColor = getBaseColorFromResource(context, R.color.white)
        val backgroundColors = if (rowIndex % 2 == 0)greyColor else whiteColor

        val labelCell = PdfPCell(Phrase(label, font)).apply {
            border = PdfPCell.NO_BORDER //BOTTOM - for bottom line
            horizontalAlignment = Element.ALIGN_LEFT
            backgroundColor = backgroundColors
            paddingBottom = 10f
        }

        val valueCell = PdfPCell(Phrase(value, font)).apply {
            border = PdfPCell.NO_BORDER
            horizontalAlignment = Element.ALIGN_LEFT
            backgroundColor = backgroundColors
            paddingBottom = 10f
        }

        table.addCell(labelCell)
        table.addCell(valueCell)
        document.add(table)
    }

}

class AddLineSeparatorAction(
    private val lineSeparator: LineSeparator
): PdfAction {
    override fun perform(document: Document) {
        document.add(Chunk(lineSeparator))
    }
}

class AddParagraphAction(
    private val text: String = "\n",
    private val alignment: Int = Paragraph.ALIGN_LEFT
): PdfAction {
    override fun perform(document: Document) {
        val paragraph = Paragraph(text)
        paragraph.alignment = alignment
        document.add(paragraph)
    }
}

class AddImagesInRowAction(
    private val context: Context,
    private val drawableId1: Int,
    private val drawableId2: Int,
    private val drawableWidth: Int,
    private val drawableHeight: Int
):PdfAction{
    override fun perform(document: Document) {
        Add2ImagePdfInRow.addImagesInRow(document, context, drawableId1, drawableId2, drawableWidth, drawableHeight)
    }
}

object Add2ImagePdfInRow {
    private fun createImage(
        context: Context,
        drawableId: Int,
        drawableWidth: Int,
        drawableHeight: Int
    ):Image{
        val drawable = ContextCompat.getDrawable(context,drawableId)!!
        val bitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0,0,canvas.width,canvas.height)
        drawable.draw(canvas)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val image = Image.getInstance(stream.toByteArray())
        image.alignment = Element.ALIGN_CENTER
        return image
    }

    fun addImagesInRow(
        document: Document,
        context: Context,
        drawableId1: Int,
        drawableId2: Int = 0,
        drawableWidth: Int,
        drawableHeight: Int
    ){
        val table = PdfPTable(2)
        table.widthPercentage = 100f

        val image1 = createImage(context,drawableId1,drawableWidth,drawableHeight)
        val image2 = createImage(context,drawableId2,drawableWidth,drawableHeight)

        val image1Cell = PdfPCell(image1)
        image1Cell.border = PdfPCell.NO_BORDER
        image1Cell.horizontalAlignment = Element.ALIGN_LEFT

        val image2Cell = PdfPCell(image2)
        image2Cell.border = PdfPCell.NO_BORDER
        image2Cell.horizontalAlignment = Element.ALIGN_RIGHT

        table.addCell(image1Cell)
        table.addCell(image2Cell)

        document.add(table)
    }
}

class AddImageAction(
    private val context: Context,
    private val drawableId: Int,
    private val drawableWidth: Int,
    private val drawableHeight: Int,
    private val alignment: Int = Element.ALIGN_LEFT
): PdfAction{
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun perform(document: Document) {
        val drawable = context.resources.getDrawable(drawableId, null)
        val bitmap = Bitmap.createBitmap(
            drawableWidth,
            drawableHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0,0,canvas.width, canvas.height)
        drawable.draw(canvas)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val image = Image.getInstance(stream.toByteArray())
        image.alignment = alignment
        document.add(image)
    }

}

fun addFooterWithLines(
    writer: PdfWriter,
    document: Document,
    context: Context,
    footerText: String,
    footerFont: Font,
    lineColor1: BaseColor,
    lineColor2: BaseColor,
    lineWidth: Float,
){
    val cb = writer.directContent
    val marginLeft = document.left()
    val marginRight = document.right()
    //footer text position
    val textBase = document.bottom() + 10f //height from bottom
    //footer text
    cb.beginText()
    cb.setFontAndSize(footerFont.baseFont, footerFont.size)
    cb.setColorFill(footerFont.color)
    cb.showTextAligned(
        Element.ALIGN_LEFT,
        footerText,
        marginLeft,
        textBase,
        0f
    )
    cb.endText()

    //firstLine position
    val line1Y = textBase - 13f //footer-text and line gap
    val line2y = line1Y - lineWidth

    //draw the first line
    cb.setLineWidth(lineWidth)
    cb.setColorStroke(lineColor1)
    cb.moveTo(marginLeft, line1Y)
    cb.lineTo(marginRight, line1Y)
    cb.stroke()

    //draw the second line
    cb.setColorStroke(lineColor2)
    cb.moveTo(marginLeft, line2y)
    cb.lineTo(marginRight, line2y)
    cb.stroke()

}

object PdfUtil {

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.Q)
    fun createAndDisplayPdf(
        context: Context,
        type: String,
        contentProvider: PdfContentProvider
    ) {

        val pdfFileName = "Receipt.pdf"
        val directoryName = "OCEAN"
        val actions = contentProvider.providePdfActions()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/$directoryName"
                val dir = File(path)
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, pdfFileName)
                val fOut = FileOutputStream(file)

                val document = Document()
                val writer = PdfWriter.getInstance(document, fOut)

                document.open()
                // Document Settings
                document.setPageSize(PageSize.A4)
                document.addCreationDate()
                for (action in actions){
                    action?.perform(document)
                }

                //add footer
                val footerFont = Font(
                    BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252,BaseFont.NOT_EMBEDDED),
                    9f,
                    Font.NORMAL,
                    BaseColor.BLACK
                )
                val lineColor1 : BaseColor = getBaseColorFromResource(context, R.color.orange)
                val lineColor2 : BaseColor = getBaseColorFromResource(context, R.color.purple)
                addFooterWithLines(
                    writer,
                    document,
                    context,
                    footerText = "NOTE: For any questions about your transaction, please reach out to company customer care at 00000000000.",
                    footerFont,
                    lineColor1,
                    lineColor2,
                    lineWidth = 5f //height for two of the lines
                )

                document.close()

                withContext(Dispatchers.Main){
                    if (type == "pdf"){
                        viewPdf(context)
                    }else{
                        sharedPdfFile(context)
                    }
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    private fun getUriForPdfFile(context: Context, pdfFile: File): Uri {
        val authority = "${context.packageName}.provider"
        return FileProvider.getUriForFile(context, authority, pdfFile)
    }

    private fun viewPdf(context: Context){
        val pdfFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "OCEAN/Receipt.pdf"
        )
        val path = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
        //setting the intent for a PDF reader
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(getUriForPdfFile(context, pdfFile),"application/pdf")
        pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        pdfIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        try {
            context.startActivity(pdfIntent)
        }catch (e: ActivityNotFoundException){
            Toast.makeText(
                context,
                "No application has been found to open PDF files.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sharedPdfFile(context: Context){
        val pdfFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "OCEAN/Receipt.pdf"
        )
        val path = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
        // Create an Intent with action ACTION_SEND
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"

        // Set the Uri of the file to be shared
        shareIntent.putExtra(Intent.EXTRA_STREAM, path)

        // Add any additional information or text
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Sharing PDF file")

        // Set flags to grant read permission to the receiving app
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            // Start the Intent to share the PDF file
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF file"))
        } catch (e: Exception) {
            // Handle any exceptions that may occur
            Toast.makeText(context, "Error sharing PDF file", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}