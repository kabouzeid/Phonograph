
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

public class Formationlist {

    @Expose
    private Formation formation;

    /**
     * @return The formation
     */
    public Formation getFormation() {
        return formation;
    }

    /**
     * @param formation The formation
     */
    public void setFormation(Formation formation) {
        this.formation = formation;
    }

}
