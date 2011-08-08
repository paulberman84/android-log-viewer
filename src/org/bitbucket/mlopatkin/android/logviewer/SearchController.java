/*
 * Copyright 2011 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.mlopatkin.android.logviewer;

import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class SearchController {

    private JTable table;
    private LogRecordTableModel model;
    private String text;
    private int curRow;

    public SearchController(JTable table, LogRecordTableModel model) {
        this.table = table;
        this.model = model;
    }

    public void startSearch(String text) {
        this.text = text;
        curRow = table.getSelectedRow();
        performSearch(curRow >= 0);
    }

    public void continueSearch() {
        performSearch(false);
    }

    private void performSearch(boolean scanCurrentRow) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        if (curRow != table.getSelectedRow()) {
            curRow = table.getSelectedRow();
        }
        int startPos = (scanCurrentRow) ? curRow : (curRow + 1);
        for (int i = startPos; i < table.getRowCount(); ++i) {
            LogRecord record = model.getRowData(table.convertRowIndexToModel(i));
            if (StringUtils.contains(record.getTag(), text)
                    || StringUtils.contains(record.getMessage(), text)) {
                setCurrentRow(i);
                return;
            }
        }
    }

    private void setCurrentRow(int i) {
        curRow = i;
        table.scrollRectToVisible(table.getCellRect(curRow, 0, false));
        table.getSelectionModel().setSelectionInterval(curRow, curRow);
        table.requestFocusInWindow();
    }

}