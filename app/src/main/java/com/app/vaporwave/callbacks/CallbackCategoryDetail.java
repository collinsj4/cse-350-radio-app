package com.app.vaporwave.callbacks;

import com.app.vaporwave.models.Category;
import com.app.vaporwave.models.Radio;

import java.util.ArrayList;

public class CallbackCategoryDetail {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public Category category = null;
    public ArrayList<Radio> posts = new ArrayList<>();

}
