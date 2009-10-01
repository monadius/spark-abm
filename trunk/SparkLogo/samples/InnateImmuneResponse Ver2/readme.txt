WHAT IS IT?
-----------

*NOTE:  This is a Netlogo version of a model that was developed in StarlogoT and used to generate data for a series of presentations and a paper that is currently under review for the Journal of Critical Care Medicine.  That model is available on the StarlogoT community model website.  The Netlogo version is as direct a port of the StarlogoT model as I can accomplish, and is posted primarly at the request of the reviewers of the above mentioned paper so that the structure and assumptions of the ABM can be directly and transparently examined by interested parties in a platform-independent venue.  However, I wish to make it clear that the data generated for that paper was not done with this version of the model, and that some of the differences in coding and any differences in the action-scheduler between StarlogoT and Netlogo may lead to discrepency between attempts to re-generate the data presented in the paper.  Furthermore, the Netlogo version runs considerably slower.  However, the qualitative behavior of the Netlogo version should be the same as the StarlogoT version, and this version should give interested parties opportunities to examine and experiment with the process of agent based modeling of the Innate Immune Response.

This is a model of the interface between the endothelial cells that line capillaries, and blood-borne inflammatory cells (white blood cell species (WBCs)) and mediators.  It is designed to simulate the processes of the innate inflammatory response (IIR).  The IIR is the component of the immune system that is the initial response to injury/infection, prior to the determination of self/non-self recognition associated with the adaptive immune response (antigen presentation/antibody formation).  Excessive activation of the IIR can lead to the disease process Systemic Inflammatory Response Syndrome (SIRS)/Multiple Organ Failure (MOF)/Sepsis, which is a leading source of morbidity and mortality in the Intensive Care Unit (ICU) environment.  One of the frustrations facing treatment of SIRS/MOF/Sepsis is the difficulty in transferring the vast increases in knowledge regarding the cellular and molecular aspects of the IIR into effective clinical regimes.  This model in intended to demonstrate how information generated from basic science experiments can be translated into a synthetic framework that approximates the behavior seen in the clinical setting (global systemic behavior).

My premise regarding SIRS/MOF/Sepsis is that it is a phase space of the IIR that has only become “uncovered” by improvements in acute resuscitation and organ support over the past half century.  In other words, patients who otherwise would have died acutely of their injuries/infections prior to the ICU era are now being kept alive and as a consequence of this the AIR is now acting beyond its “design parameters,’” with the result that the actions of the AIR are now detrimental to the body.  I presume that incremental increase of the initial perturbation to the system will map out the possible dynamic states that correspond to  observed clinical behaviors.

The topology of the model is a torus, which approximates the internal surface of all the capillary beds in the body.  There is no directional flow, and movement of the WBCs “wraps” at the edges.  The patches are Endothelial Cell (EC) agents, the turtles are the various species of WBCs (PMNs, Monos and T-cells).  There is no tissue differentiation.  For a more complete description of the various agents and variables in the model see Appendix B at the end of this section.


HOW IT WORKS
------------

The following are Appendix B and C from the submitted paper mentioned above.  If/when the paper is published, the reference will be added to the web site.

Appendix B: Descriptions of Cell Types, Rules Systems and Mediator Determination

The following are descriptions of the agents and variables in the current series of models.  The descriptions are given in plain English, not computer code.  However, some of the algebraic relationships between the various variables are given.  It must be emphasized that the values for the variables are non-scalar (unit-less).  Furthermore, the relationships are modeled using only simple arithmetic.  We understand that this is extremely abstracted.  However, given that complex systems generate their nonlinear behavior through the aggregation of large numbers on essentially linear interactions we believe that the overall dynamics simulated are qualitatively valid.

Terminology Note:  "Patch" variables refer to variable held on a particular grid coordinate.  ""Agent" variables refer to variables held on a mobile agent ("Turtles" in the terminology of StarlogoT).  The term "update" is used in the following section to mean the calculation that the updating agent uses to determine the value of the variable being updated.  Implicit to all updating is that the current value of the variable on that patch is included.

Time Scales:
1 step = 7 minutes
1 time = 100 steps = 700 minutes = 11.67 hours
1 loop = 58 time = 5800 steps = 40600 minutes = 28.19 days


Patch variables representing mediators and their effects (NOTE: Despite their names, the actions of the variables are admittedly incomplete):
1. "Endotoxin" is produced by simulated infectious vectors (see below).  It activates ECs, PMNs and Monos.  It is a component of chemotaxis for PMNs.  It is also incorporated into the calculation of "TNF," "IL-1" and "GCSF."
2. "Cytotox" represents bactericidal free radical species produced in respiratory burst.  A full description of its production and function is below.
3. "PAF" is produced by activated ECs.  It is a component of chemotaxis for PMNs and Monos.  It is incorporated into the calculations of "TNF" and "IL-1."
4. "TNF" is produced by both PMNs and Monos.  "TNF" is incorporated into the calculations for "IL-8," "IL-10," "IL-12," "GCSF," and "INF-g."  Its calculation is described extensively below.  "TNF" also affects PMN activation, adhesion, migration and apoptosis.
5. "IL-1" is produced by both PMNs and Monos.  "IL-1" is incorporated into the calculations for "IL-8," "IL-10," "IL-12," "GCSF," and "INF-g."  Its calculation is described extensively below.  "IL-1" also affects PMN adhesion.
6. "IL-4" is produced by TH2 cells and promotes transition of TH0 cells to TH2 cells.
7. "IL-8" is produced by Monos and is chemotactic for PMNs.
8. "IL-10" is produced by Monos and TH2 cells.  It affects PMN migration and apoptosis, activation status of Monos, and the transition of TH0 to TH2 cells.  It is included in the calculations of "IL-10" and "IL-4."
9. "IL-12" is produced by TH1 cells.  It promotes the transition of TH0 to TH1 cells.  It is included in the calculations of "IL-12" and "IFN-g."
10. "INF-g" is produced by TH1 cells.  It affects PMN apoptosis, Mono activation and transition of TH0 to TH1 cells.  It is involved in the calculation of GCSF and TNF.
11. "GCSF" is produced by Monos.  It affects the rate of production of PMNs.  Its apoptotic and direct cytokine effects have not been included.

Endothelial Cells (EC)
     Non-mobile agents imbedded in the grid-space of the model.  In mode =1 they are scaled to "oxy" and appear as red squares.  There are 10201 ECs in the system (grid is 101 x 101).  They hold an agent-state variable labeled "oxy" which simulates their "health" status.  Oxy ranges from 0 to 100.  Oxy is decreased by various types of injury.  Furthermore, to simulate the effect of downstream ischemia, ECs affect surrounding ECs in a cellular automata (CA) fashion.  Ischemia updates utilize a Moore neighborhood of effect, and have a bimodal effect based on ischemia threshold (30 < oxy < 60) and an infarct threshold (oxy =< 30).  At ischemia, "oxy" is reduced by 0.05.  At infarction, "oxy" is reduced by 0.25.
     ECs also have agent-state variables that correspond to adhesion receptors, specifically E/P-Selectin and ICAM. P-Selectin initiated at 20 min after activation, peaks at 4hrs; E-Selectin initiated at 4 hr after activation, peaks at 24 hr; ICAM-1 initiated at 12 hr and peaks at 24 hr.  P and E Selectin functions are combined into a single EC state variable "ec-roll", and are activated at ec-roll = 3 (updates at 1 / step).  ICAM is represented by "ec-stick," which activates at ec-stick = 100 (updates 1 / step). In situations where ECs are in infarction state (oxy =< 30), ec-stick is automatically set to 100; this simulates WBC adhesion to grossly damaged ECs.
ECs also produce variables that correspond to mediators, specifically PAF and IL-8.
ECs are activated by Endotoxin >= 1 or oxy < 60.  EC-activated will:
1) Increase ec-roll by 1
2) Increase ec-stick by 1
3) Increase PAF by 1
4) Produce IL-8 at IL-8 + TNF + IL-1
The threshold for activation by endotoxin prevents total activation with endotoxin challenge.  The same goes for oxy < 60 in face of future ischemia/re-perfusion simulations.
ECs in the current model do not heal themselves; it requires activation of the IIR to do so.

Infection Simulation
     Infectious mode simulates infectious agent with a patch variable.  This is done instead of using "turtles" due to the limitation on the number of "turtles" allowed by StarlogoT.  Therefore, "infection" is a patch variable that updates in a CA fashion. It increases the home patch "infection" by 0.1, value capped at 100.  The presence of infection vectors is represented by grey flashing patches (see above).  Once this level is reached the CA updates in a random Moore neighborhood by increasing "infection" + 1.  This simulates the spread of infection to adjacent patches.
     The "infection" variable is also included in the determination of the current "oxy" level by the ECs.  It reduces the "oxy" level by its own value, "infection".  The infection function also updates/produces a patch variable that simulates endotoxin.  "Endotoxin" is updates by + "infection" / 10.  "Infection" is reduced by patch variable "cytotox."  This variable is updated by PMNs and Monos (see below), and simulates the effect of free radicals produced in white blood cell respiratory burst.  "Infection" is decreased by "cytotox" per step.
      In order to simulate ongoing challenges to the simulated IIR there is recurrent baseline infection occurs every 100 steps (at step=99).  Each challenge produces 5 random patches of infection 100.

Neutrophils (PMNs)
     The initial number of PMNs is 500.  They are the white turtles.  PMNs in a non-perturbed system last approximately 6 hrs (50 steps).  Agents that simulate bone marrow/PMN progenitor cells replenish the pool of PMNs. A non-perturbed system has a steady state number of PMNs at 500.  In this version of the model progenitor cells are affected by "GCSF" in a forward feedback fashion.
     PMNs are mobile agents.  Since the model does not have directional flow, in a non-perturbed system the PMNs move two grid spaces per step in a random, 360-degree fashion.  However, in perturbed systems the PMNs will follow a gradient of variables that simulate chemotactic factors.  This model uses a combination of "PAF," "endotoxin" and "IL-8" as the PMN chemotactic factors.
     PMNs have a series of actions; all linked to their own agent variables, EC variables and mediator variables.
1. PMN-roll.  This function represents initial PMN activation and rolling.  The PMNs have an agent variable called "pan-roll."  This is set to 1, representing existing L-Selectin on the surface of PMNs.  The activating variables correspond to endotoxin, PAF and TNF.  If the PMN happens to move over an EC that has been activated such that "ec-roll"=3, then the PMN will halve its movement rate. The PMN also has an agent variable called ‘pan-stick."  This corresponds to CD-11/18 integrens and is initially set to 0. The PMN checks to see if patch variables "TNF" and "PAF" are present, then the PMN will set "pmn-stick" to a value equal to "IL-1."  The PMN will also update "IL-1ra" at + 1.
2. PMN-stick.  This function represents the next step in PMN adhesion. The PMNs have an agent variable called "pmn-migrate."  This variable determines the ability of the PMN to diapedis.  If the PMN’s "pmn-stick" >= 1 and it happens to move over an EC in which its "ec-stick" >= 100, then the PMN will cease moving. Adhesed PMNs will turn yellow.  The PMN will set "pmn-migrate" equal to "TNF" plus "IL-1" minus "IL-10."
3. PMN-migrate.  If the PMN’s "pmn-migrate" is greater than 0, then this PMN will have been considered to have migrated through the EC surface and will execute its next function, PMN-burst.
4. PMN-burst.  This function simulates PMN respiratory burst and thus its cytotoxic/bactericidal effect.  It does this primarily by updating a patch variable called "cytotox."  In the presented model this represents overall free radical species (subsequent models will differentiate these species).  It is updated at a value of 10 or "TNF," whichever is greater.  "Cytotox" has the following functions:
1. It reduces "infection" by "cytotox."  This is the bactericidal effect.
2. It reduces "oxy" by "cytotox."  This is the cytotoxic effect on otherwise undamaged ECs.
5. The PMN also updates "TNF" and "IL-1" by 1.
6. The PMN will also "heal" the EC it happens to be sitting on.  This simulates phagocytosis of bacteria and debris of damaged ECs.  It will set the underlying EC’s "oxy" back to 100 and all its adhesion variables back to baseline.
7. Finally, PMN’s have a simulated apoptotic function.  The counter that determines its life span is affected by cytokine variables.  The PMN life span is increased by "TNF" and "IFN-g," and decreased by "IL-10."

Monocytes/Macrophages (Monos)
     The initial number of Monos is 50. Monos are Green turtles.  Monos in a non-perturbed system last approximately 6 hrs (50 steps).  Agents that simulate bone marrow/mono progenitor cells replenish the pool of Monos. A non-perturbed system has a steady state number of Monos at 50.  This version of the model does not have GMCSF therefore there is no feedback mechanism to increase or decrease Mono production.
     Monos in this model are all circulating cells (there are no static tissue macrophages). However, once Monos undergo adhesion and tissue migration the macrophage function of phagocytosis/debris removal is incorporated into their function.  In a non-perturbed system the Monos move two grid spaces per step in a random, 360-degree fashion.  However, in perturbed systems the Monos will follow a gradient of variables that simulate chemotactic factors.  This model uses PAF as the Mono chemotactic variable.
     Monos have a rolling/sticking/migration process similar to PMNs.  The primary difference, however, is that they do not have a "burst" function.  Rather, Monos have an "activation" status that is not limited to their adhesion status (more on this below).
     The Monos also have agent variables that correspond to TNF receptors ("TNFr") and IL-1 receptors ("IL-1r").  These two receptors determine the degree of responsiveness of the Mono in the production of these two cytokines.  IL-1r is has the following characteristics:
1. "IL-1r" is capped at 100.
2. "sIL-1r" is the shed form of the receptor, and is produced by Monos. The equation for this is "sIL-1r" + ("IL-1r" / 2).
3. Monos also produce "IL-1ra" (previously mentioned in "pmn-roll."  It is produced at "IL-1ra" + ("IL-1" / 2).
4. "IL-1r" determined by the Mono as "IL-1" – "IL-1ra" – "sIL-1r."  This reflects competitive binding between IL-1 and IL-1ra and sIL-1r to IL-1 receptors.
      "TNFr" has the following characteristics, a little more complicated because of its bimodal effect:
1) "TNFr" is capped at 100.
2) "sTNFr" is the shed form of the receptor produced by Monos.  The equation for "sTNFr" is "sTNFr" + ("TNFr" / 2).
3) If "sTNFr" has a relatively low value (<= 100), it increases "TNFr" activity (and thus "TNF" production) as the minimum number between 100 and ("TNF" + "sTNFr").  This reflects the potentiating effect of sTNFr at low concentrations for TNF binding to TNF receptors.
4) If "sTNFr" is a high number (> 100), then it reduces "TNFr" activity (and thus "TNF" production) as the maximum number between 0 and ("TNF" – "sTNFr") (this is done to prevent negative values).  This reflects competitive binding between TNF and sTNFr for TNF receptors at high levels of sTNFr.
     The "activation" status is responsible for determining which cytokines and how much they produce each step.  "Activation" is therefore an agent variable that is determined by the relationship of the following patch variables on the given patch: "PAF" + "Endotoxin" + "INF-g" – "IL-10."
If "activation" is greater than 0, then the Monos is thought of as "Pro-inflammatory."  The following functions occur, in the following sequence:
1. Update "GCSF" by ("endotoxin" + "IL-1" + "TNF" + "INF-g") / 4
2. Update "IL-8" by "TNF" + "IL-1"
3. Update "IL-12" by ("TNF" + "IL-1") / 2
4. Update "IL-10" by ("TNF" + "IL-1") / 2
5. Update "IL-1" by "endotoxin" + "PAF" + "IL-1r"
6. Update "TNF" by "endotoxin" + "PAF" + "TNFr" + "INF-g"
7. If the Mono has migrated, then it will "heal" as in "pmn-burst."
If "activation" status is less than 0, then the Mono is considered to be "Immune-suppressed."  The following functions occur, in the following sequence:
1. Update "IL-10" by (TNF + IL-1) / 2
2. If the Mono has migrated, then it will have a 50% chance of performing "heal."  This reflects diminished phagocytic activity in suppressed Monos.
There is currently no apoptotic function for Monos.

TH-cells (TH0, TH1, TH2)
     TH1 cells represent the pro-inflammatory T-cells; they are Blue.  TH2 cells represent anti-inflammatory T-cells; they are Cyan.  TH0 cells represent progenitor cells for the two cell types above; they are Violet.  Initially there are 50 TH1s and 50 TH2s.  Production of these is kept such that both populations have a steady state ~ 50 in non-perturbed systems.  There are no initial TH0s, but these are produced to reach a steady state of ~100 and have no function in non-perturbed systems.  However, once an insult has occurred and mediators have begun to be produced, the TH0 population will switch into either the TH1 population or the TH2 population.  This is intended to reflect the pro-inflammatory/anti-inflammatory balance determined by these TH cell populations.

Differentiation from TH0 is determined in the following fashion:
1. Differentiation is linked to an "activation" agent variable.
2. If random (("IL-12" + "INF-g") * 100) > random (("Il-4" + "IL-10") * 100), then activation increases by 1.
3. If random (("IL-10" + "IL-4") * 100) > random (("IL-12" + "INF-g") * 100), then activation decreases by 1.
  Note:  The mediator values are multiplied by 100 prior to random determination due to the fact that the RNGs in Starlogo only generate integer values.
4. When activation >=10 then the TH0 changes to TH1.
5. When activation <=-10 then the TH0 changes to TH2.

TH1 cells are activated in the presence of "IL-12."  If "IL-12" is greater than 0, then they will update "IFN-g" by "IL-12" + "TNF" + "IL-1."  They also update "IL-12" by "IL-12."

TH2 cells are activated in the presence of "IL-10."  If "IL-10" is greater than 0, then they will update "IL-4" by "IL-10" and also update "IL-10" by "IL-10."

I wish to emphasize again that the listed agents and variables are not intended to be comprehensive in terms of either content or effect, but represents the current level of the model.  This appendix is intended not only to explain the content of the model, but also demonstrate how these rule systems can be generated.  I fully realize the scope of this project, and given that would welcome all and any input into the further refinement of the agent types and rules for a more comprehensive model.

Appendix C:  Descriptions of Mediator-Directed Interventions and References

1. 1 dose anti-TNF.  Effect of therapy is programmed as reduction of TNF level to 10% control levels for 2-hrs duration after dose given at 12-hrs post injury.  Subsequently TNF is produced and updated in the usual fashion.  Reference 1.
2. 3 days anti-TNF.  Effect of therapy is programmed based on reduction of TNF by drug variable "TNFmab" for therapy initiated 12-hrs post injury and maintained for 72 hrs.  Level of "TNFmab" determined to be able to decrease "TNF" levels to 10% compared to baseline mean values.  Reference 2.
3. 3 days anti-IL-1.   Effect of therapy is programmed as complete binding of IL-1r on Monos (done by setting "IL-1r" to 0) for 72 hrs, starting at time 12-hrs post injury.  Reference 3.
4. 1 dose anti-CD-18.  Effect of therapy is programmed as being initiated at 6 hrs post injury (sterile mode only) and being initially 90% likelihood "pmn-stick" set to 0 for first 48 hrs, then 50% likelihood "pmn-stick" set to 0 for second 48 hrs.  Reference 4.
5. 3 days of anti-TNF and IL-1.  Effect of therapy is programmed as being initiated at 12-hrs post injury and combines the effects of #2 and #3 above. Reference 43.
6. Hypothetical Multi-modal 3 days of anti-TNF/anti-IL-1 and 1 dose anti-CD-18.  Effect of therapy is programmed as being initiated at 12-hrs post injury and combines the effects of #2, #3 and #4 above.
7. Hypothetical 7 days of Low Dose anti-TNF.  Effect of therapy programmed as being initiated as beginning at 12-hrs post injury and setting drug variable "TNFmab" at half the dose given in intervention #2 for 7 days simulated time.
8. Hypothetical 7 days of Low Dose anti-IL-1.  Effect of therapy programmed as being initiated as beginning at 12-hrs post injury and having there be a 50% likelihood that "IL-1r" on Monos would be set to 0 for 7 days of simulated time..


HOW TO USE IT
-------------
SLIDERS:  There are 3 sliders:

"Inj-number":  This is the amount of initial injury to the system.  It represents the primary independent variable in the model (IIN).

"Mode":  This slider determines which patch variable the patch color is scaled to.  The primary mode is “1,” which is the EC life variable “oxy.”  The other modes are listed below:
1= Oxy (Injury)
2= Endotoxin
3= PAF
4= cytotox
5= TNF
6= IL-1
7= sTNFr
8= INF-g
9= IL-10
10= IL-1ra
11= IL-8
12= IL-12
13= IL-4
14= GCSF


BUTTONS:
"Setup":  Resets model.
"Injure-Sterile":  Injures the system set IIN without infection simulated
"Injure-Infection":  Injures the system set IIN with infection simulated
"Go":  Runs the model

SWITCHES:
"Graph":  This turns on the Draw Graph function.

BASIC OPERATION OF THE MODEL:

Determine the IIN by adjusting the the “Inj-number” slider.

Press “Setup,” this will reset the model and all the variables except the global variable “loop” (more on this later).

If concurrent graphs are desired, turn the "Graph" switch to "On." (Note, the graphs generated thusly are not the graphs in the paper; those were generated by exporting data collected in the output window).

Press either “Injure-Sterile” or “Injure-Infection” to deliver initial injury.

Press either “Go."

The model will then run.  EC damage/death will be reflected in Mode=1 as a “blackening” of the affected ECs.  In other modes the color of the patches will reflect the level of the underlying mediator variable.  Of note, when there are active infectious vectors the patches will “flash” grey; this is because the presence of infectious vectors is measured as a patch variable and since the patches will scale only to one variable at a time it will “flash” between the grey color and whatever other color the patch is scaled to.

“MODE” OF THIS VERSION OF THE MODEL:

The model is written with the random number generators (RNGs) unseeded.  The model should have the RNGs seeded if one is to plan modifications or run distributions.  The model is not currently set up to run distributions; rather it is set up to produce populations of n=100 runs and then increase the IIN by 50 for another n=100 runs, and so on.  The “n” of the run is reflected by the “loop” variable (which is not reset to 0 with “Setup.”  “Loop” must be manually reset to 0 in the Command Center.
The model is also set up to run in “infection mode.”  Meaning that repeated loops are reset with initial perturbation “Injure-Infection” in “to go.”  To run in “sterile mode” this command line needs to be changed to “injure-sterile.”
Output to Output Window is “oxy-deficit,” “total-infection,” and “time” for each loop.

RECREATING ASPECTS OF THE PAPER:

The following instructions are directed at reproducing the forms of certain runs mentioned in a paper submitted to the Journal of Critical Care Medicine (in review).

Population Runs were made using the base mode presented here.

Distribution Runs:  The RNGs need to be seeded.  In “to go1” instead of increasing the “loop” at the end of one run, the IIN is increased by 50 and the system is restarted.  Output should be modified to record IIN, oxy-deficit, total-infection and time at the end of each run.

Individual Behavior Runs:  These are the runs that generate the graphs “Heal,” “Phase I SIRS,” Phase II MOF,” and “Overwhelming Infection/Injury.”  Seed the RNGs, place the Data-collection slider to 1.  Output window will record sequential time points during a single run.

Cytokine Profiles:  Use the data-collection slider to 1, un-seed the RNGs, and run the model at a fixed IIN for “n” number of loops.

For simulation of antibiotics/anti-cytokine regimes, see the appendices at the end of this information window.


THINGS TO NOTICE/THINGS TO DO:

Notice how the response seems to form an “abcess cavity” around the injury pattern.  System death occurs when this containment is broken/inadequate.

Notice after watching a few runs how you can tell which systems will “live” and which will “die.”  What are the specific tip off to each out come, if any?

Notice whether patterns emerge as to whether the forward feedback loops (pro-inflammatory pathways) are driving the ongoing damage, or it is the negative feedback loops (immune suppression) that prevent clearance of “second hits.”

Notice if you try to simulate single doses of any anti-mediator therapy how long the effect appears to last (despite the half-life of the drug).  Note that the base model is fairly robust, and this robustness also pertains to its pathologic states.

People are encouraged to tinker with diffusion/evaporation constants, various formulas for cytokine determination and different characteristics of infection vectors (endo versus exotoxins).


CREDITS AND REFERENCES
----------------------

QUESTIONS AND COMMENTS REGARDING THIS MODEL CAN BE SENT TO:
Gary An, MD
Department of Trauma, Cook County Hospital
<<DOCGCA@AOL.COM>>
