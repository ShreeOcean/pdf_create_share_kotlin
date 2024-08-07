package com.ocean.pdfcreateviewshareapp.itextpdf

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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
import com.itextpdf.text.Rectangle
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

class AddDataItemAction(
    private val label: String,
    private val value: String,
    private val font: Font,
): PdfAction {
    override fun perform(document: Document) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        val labelCell = PdfPCell(Phrase(label, font)).apply {
            border = PdfPCell.NO_BORDER //BOX
            horizontalAlignment = Element.ALIGN_LEFT
        }
        val valueCell = PdfPCell(Phrase(value, font)).apply {
            border = PdfPCell.NO_BORDER
            horizontalAlignment = Element.ALIGN_LEFT
        }
        table.addCell(labelCell)
        table.addCell(valueCell)
        document.add(table)
//        document.add(Paragraph("$label: $value", font))
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

interface PdfContentProvider{
    fun providePdfActions(): List<PdfAction?>
}

fun Document.addRectangle() {
    val rect = Rectangle(577f, 825f, 18f, 15f)
    rect.enableBorderSide(1)
    rect.enableBorderSide(2)
    rect.enableBorderSide(4)
    rect.enableBorderSide(8)
    rect.border = Rectangle.BOX
    rect.borderWidth = 2f
    rect.borderColor = BaseColor.LIGHT_GRAY
    this.add(rect)
}

fun Document.addImage(
    drawable: Drawable,
    drawableWidth: Int,
    drawableHeight: Int
) {
    val bitmap = Bitmap.createBitmap(
        drawableWidth,
        drawableHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

    val image = Image.getInstance(stream.toByteArray())
    image.alignment = Element.ALIGN_CENTER
    this.add(image)
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
                PdfWriter.getInstance(document, fOut)

                document.open()
                // Document Settings
                document.setPageSize(PageSize.A4)
                document.addCreationDate()
                document.addRectangle()
                // Convert the drawable to an Image
                document.addImage(ContextCompat.getDrawable(context, R.drawable.ic_android_black_24dp)!!,200,75)

                for (action in actions){
                    action?.perform(document)
                }

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
//        pdfIntent.setDataAndType(path,"application/pdf")
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