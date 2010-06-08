package gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Very simple graphical console
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Console extends JScrollPane
{
    private JTextArea   textArea;

    public Console()
    {
        textArea = new JTextArea(10, 10);
        setViewportView(textArea);

        textArea.setEditable(false);
        System.setOut(new PrintStream(new MyOutputStream()));
        System.setErr(new PrintStream(new MyOutputStream()));
    }

    public synchronized void clearText()
    {
        textArea.setText("");
    }

    public synchronized void printLine(String str)
    {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public synchronized void printChar(char ch)
    {
        textArea.append(String.valueOf(ch));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    class MyOutputStream extends OutputStream
    {
        public void write(int b) throws IOException
        {
            printChar((char) b);
        }
    }
}
