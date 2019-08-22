package org.esa.snap.product.library.ui.v2;

import org.esa.snap.product.library.v2.ProductLibraryItem;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by jcoravu on 21/8/2019.
 */
public class QueryProductResultsPanel extends JPanel {

    private final JLabel titleLabel;
    private final JList<ProductLibraryItem> productList;
    private final ActionListener downloadProductListener;

    public QueryProductResultsPanel(ActionListener downloadProductListener) {
        super(new BorderLayout());

        this.downloadProductListener = downloadProductListener;
        this.titleLabel = new JLabel(getTitle());
        this.productList = new JList<ProductLibraryItem>(new ProductListModel());
        this.productList.setCellRenderer(new ProductListCellRenderer());
        this.productList.setVisibleRowCount(4);
        this.productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.productList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                productListMouseClicked(mouseEvent);
            }
        });

        add(this.titleLabel, BorderLayout.NORTH);
        add(new JScrollPane(this.productList), BorderLayout.CENTER);
    }

    public ProductLibraryItem getSelectedProduct() {
        return this.productList.getSelectedValue();
    }

    public void addProducts(List<ProductLibraryItem> products) {
        ProductListModel productListModel = (ProductListModel)this.productList.getModel();
        productListModel.addProducts(products);
        this.titleLabel.setText(getTitle() + ": " + productListModel.getSize());
    }

    public void setProductQuickLookImage(ProductLibraryItem product, Image quickLookImage) {
        ProductListModel productListModel = (ProductListModel)this.productList.getModel();
        productListModel.setProductQuickLookImage(product, quickLookImage);
    }

    public void clearProducts() {
        ProductListModel productListModel = (ProductListModel)this.productList.getModel();
        productListModel.clearProducts();
        this.titleLabel.setText(getTitle());
    }

    private void productListMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            int clickedProductIndex = this.productList.locationToIndex(mouseEvent.getPoint());
            if (clickedProductIndex >= 0) {
                Rectangle cellBounds = this.productList.getCellBounds(clickedProductIndex, clickedProductIndex);
                if (cellBounds.contains(mouseEvent.getPoint())) {
                    if (!this.productList.getSelectionModel().isSelectedIndex(clickedProductIndex)) {
                        this.productList.getSelectionModel().setSelectionInterval(clickedProductIndex, clickedProductIndex);
                    }
                    showProductsPopupMenu(mouseEvent.getX(), mouseEvent.getY()); // right mouse click
                }
            }
        }
    }

    private void showProductsPopupMenu(int mouseX, int mouseY) {
        JMenuItem downloadSelectedMenuItem = new JMenuItem("Download");
        downloadSelectedMenuItem.addActionListener(this.downloadProductListener);

        JPopupMenu popup = new JPopupMenu();
        popup.add(downloadSelectedMenuItem);

        popup.show(this.productList, mouseX, mouseY);
    }

    private String getTitle() {
        return "Product results";
    }
}
