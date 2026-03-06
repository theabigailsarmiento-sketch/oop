package util;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class NumericLimitFilter extends DocumentFilter {
    private final int maxLength;

    public NumericLimitFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String digits = text.replaceAll("[^\\d]", "");
        int currentLength = fb.getDocument().getLength();
        int newLength = currentLength - length + digits.length();

        if (newLength <= maxLength) {
            super.replace(fb, offset, length, digits, attrs);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}