package jp.tomorrowkey.intellij.injectlogtag;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author tomorrowkey
 */
public class InjectLogTagAction extends BaseGenerateAction {

    public InjectLogTagAction() {
        super(new GenerateInjectLogHandler());
    }

    static class GenerateInjectLogHandler implements CodeInsightActionHandler {

        @Override
        public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
            final Document document = editor.getDocument();
            CaretModel caretModel = editor.getCaretModel();
            final PsiClass psiClass = getPsiClass(editor, psiFile);
            if (psiClass == null) {
                warning("Inject LOG_TAG", "Caret must be in a class.");
                return;
            }

            if (findFieldByName(psiClass, "LOG_TAG") != null) {
                warning("Inject LOG_TAG", "LOG_TAG is already in the class.");
                return;
            }

            int currentLine = caretModel.getLogicalPosition().line;
            int line = findPositionOfClassDeclarationLine(document, currentLine);
            if (line == 0) {
                warning("Inject LOG_TAG", "Not found the position of class declaration.");
                return;
            }

            caretModel.moveToLogicalPosition(new LogicalPosition(line + 1, 0));
            final int offset = caretModel.getOffset();

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    String code = generateLogTagCode(psiClass);
                    document.insertString(offset, "\n");
                    document.insertString(offset, code);
                    document.insertString(offset, "\n");
                }
            });
        }

        private PsiField findFieldByName(PsiClass psiClass, String fieldName) {
            PsiField[] fields = psiClass.getFields();
            for (PsiField field : fields) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            return null;
        }

        private int findPositionOfClassDeclarationLine(Document document, int currentLine) {
            String documentText = document.getCharsSequence().toString();

            for (int i = currentLine; i >= 0; i--) {
                int lineStartOffset = document.getLineStartOffset(i);
                int lineEndOffset = document.getLineEndOffset(i);
                String line = documentText.subSequence(lineStartOffset, lineEndOffset).toString();
                if (line.indexOf(" class ") >= 0) {
                    return i;
                }
            }

            return 0;
        }

        private PsiClass getPsiClass(Editor editor, PsiFile psiFile) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement psiElement = psiFile.findElementAt(offset);
            PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
            return psiClass;
        }

        private String generateLogTagCode(PsiClass psiClass) {
            return String.format("private static final String LOG_TAG = %s.class.getSimpleName();", psiClass.getName());
        }

        private void warning(String title, String message) {
            Notification notification = new Notification("inject-log-tag", title, message, NotificationType.WARNING);
            Notifications.Bus.notify(notification);
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }

    }

}
