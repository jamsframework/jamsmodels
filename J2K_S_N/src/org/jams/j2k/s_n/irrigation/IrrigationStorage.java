/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.irrigation;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import org.jams.j2k.s_n.crop.J2KSNCrop;

/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(title = "IrrigationStorage",
author = "c6gohe2",
description = "IrrigationStorage calculation of irrigation demand")
public class IrrigationStorage extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current organic fertilizer amount")
    public JAMSInteger RotPos;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current hru object")
    public JAMSEntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in [-] plant growth water stress factor")
    public JAMSDouble wstrs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum MPS")
    public JAMSDoubleArray maxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU attribute maximum LPS")
    public JAMSDoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of MPS")
    public JAMSDoubleArray satMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU state var saturation of LPS")
    public JAMSDoubleArray satLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU crop class")
    public JAMSDouble irrigation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU crop class")
    public JAMSDouble irrigationsum;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Waterstress thershold 0-1 (-)")
    public JAMSDouble wst_thr;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Irrigation multiplier (-)")
    public JAMSDouble param2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "reduction factor for dripper irrigation (-)")
    public JAMSDouble dripperfactor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU crop class")
    public JAMSDouble cropid;

    //Berechnung
    public void run() throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity entity = entities.getCurrent();
        double wst = wstrs.getValue();
        double satMPSArray[] = satMPS.getValue();
        double maxMPSArray[] = maxMPS.getValue();
        double satLPSArray[] = satLPS.getValue();
        double maxLPSArray[] = maxLPS.getValue();
        double wstthr = wst_thr.getValue();
        double param = param2.getValue();
        double irrsum = irrigationsum.getValue();
        double irr = 0;
        double cropID = cropid.getValue();
        double red = 0;

        if (cropID == 12) {
            irr = 0;
        } else {
            if (cropID == 98) {
                irr = 0;
            } else {
                if (wst <= wstthr) {
                    for (int i = 0; i < maxMPSArray.length; i++) {
                        irr += ((maxMPSArray[i] - (satMPSArray[i] * maxMPSArray[i])) * param);
                        irr += ((maxLPSArray[i] - (satLPSArray[i] * maxLPSArray[i])) * param);
                    }

                } else {
                    irr = 0;
                }

            }
        }

        if (cropID == 8) {
            red = (1 - wstthr);
            wstthr = wstthr + ((red / 2));

            if (wst <= wstthr) {
                for (int i = 0; i < maxMPSArray.length; i++) {
                    irr += ((maxMPSArray[i] - (satMPSArray[i] * maxMPSArray[i])) * param);
                    irr += ((maxLPSArray[i] - (satLPSArray[i] * maxLPSArray[i])) * param);

                }
                irr = irr * dripperfactor.getValue();
            }
        }
        irrsum = irrsum + irr;
        irrigation.setValue(irr);
        irrigationsum.setValue(irrsum);
    }
}
