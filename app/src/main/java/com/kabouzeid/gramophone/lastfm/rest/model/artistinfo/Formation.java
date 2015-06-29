
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

public class Formation {

    @Expose
    private String yearfrom;
    @Expose
    private String yearto;

    /**
     * @return The yearfrom
     */
    public String getYearfrom() {
        return yearfrom;
    }

    /**
     * @param yearfrom The yearfrom
     */
    public void setYearfrom(String yearfrom) {
        this.yearfrom = yearfrom;
    }

    /**
     * @return The yearto
     */
    public String getYearto() {
        return yearto;
    }

    /**
     * @param yearto The yearto
     */
    public void setYearto(String yearto) {
        this.yearto = yearto;
    }

}
