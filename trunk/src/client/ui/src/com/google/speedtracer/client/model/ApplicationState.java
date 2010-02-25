/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.speedtracer.client.model;

import com.google.speedtracer.client.timeline.Constants;
import com.google.speedtracer.client.util.IterableFastStringMap;
import com.google.speedtracer.client.util.IterableFastStringMap.IterationCallBack;
import com.google.speedtracer.client.visualizations.model.HintletReportModel;
import com.google.speedtracer.client.visualizations.model.NetworkTimeLineModel;
import com.google.speedtracer.client.visualizations.model.NetworkVisualization;
import com.google.speedtracer.client.visualizations.model.SluggishnessModel;
import com.google.speedtracer.client.visualizations.model.SluggishnessVisualization;
import com.google.speedtracer.client.visualizations.model.VisualizationModel;
import com.google.speedtracer.client.visualizations.view.HintletReport;

/**
 * This class contains the state of all of visualizations.
 */
public class ApplicationState {
  private final DataModel dataModel;

  private double firstDomainValue;

  private double lastDomainValue;

  private final IterableFastStringMap<VisualizationModel> visualizationModelMap;

  public ApplicationState(DataModel sourceModel) {
    this.dataModel = sourceModel;
    visualizationModelMap = new IterableFastStringMap<VisualizationModel>();
    firstDomainValue = 0;
    // this defaults to Constants.DEFAULT_GRAPH_WINDOW_SIZE in the future
    lastDomainValue = Constants.DEFAULT_GRAPH_WINDOW_SIZE; // endTime;
    populateVisualizationModelMap(sourceModel);
  }

  public void detachFromSourceModels() {
    visualizationModelMap.iterate(new IterationCallBack<VisualizationModel>() {

      public void onIteration(String key, VisualizationModel vModel) {
        vModel.detachFromSourceModel();
      }

    });
  }

  public DataModel getDataModel() {
    return dataModel;
  }

  public double getFirstDomainValue() {
    return firstDomainValue;
  }

  public double getLastDomainValue() {
    return lastDomainValue;
  }

  /**
   * Returns a {@link VisualizationModel} given a specified key.
   * 
   * @param visualizationTitle the key used to lookup the
   *          {@link VisualizationModel}
   * @return the VisualizationModel found or null if not found
   */
  public VisualizationModel getVisualizationModel(String visualizationTitle) {
    return visualizationModelMap.get(visualizationTitle);
  }

  /**
   * This method finalizes this ApplicationState and initializes the new one
   * from the current ending state of each VisualizationModel.
   * 
   * It also iterates through the VisualizationModels and detaches them from
   * their source models. This effectively makes this ApplicationState
   * immutable, telling the VisualizationModels to no longer listen to updates
   * from its source model.
   */
  public void handOffToNewApplicationState(final String newUrl,
      final ApplicationState newState) {
    detachFromSourceModels();
    // We do a separate iteration because we want to have completely detached
    // all the previous visualization models first
    visualizationModelMap.iterate(new IterationCallBack<VisualizationModel>() {

      public void onIteration(String key, VisualizationModel vModel) {
        vModel.transferEndingState(ApplicationState.this, newState, newUrl);
      }

    });

    // The beginning of newState is either 0, or some value set by one of the
    // Visualizations in the iteration above. We set the end to be some
    // convenient window size. Either the default window size, or the last data
    // point we saw in this old state
    double end = Math.max(getLastDomainValue(), newState.getFirstDomainValue()
        + Constants.DEFAULT_GRAPH_WINDOW_SIZE);
    newState.setLastDomainValue(end);
  }

  public void setFirstDomainValue(double firstDomainValue) {
    this.firstDomainValue = firstDomainValue;
  }

  public void setLastDomainValue(double lastDomainValue) {
    this.lastDomainValue = lastDomainValue;
  }

  /**
   * Populates our VisualizationModel map with all known VisualizationModels.
   * 
   * @param sourceModel the {@link DataModel} that all the
   *          {@link VisualizationModel}s register to
   */
  private void populateVisualizationModelMap(DataModel sourceModel) {
    visualizationModelMap.put(SluggishnessVisualization.TITLE,
        new SluggishnessModel(sourceModel));
    visualizationModelMap.put(NetworkVisualization.TITLE,
        new NetworkTimeLineModel(sourceModel));
    visualizationModelMap.put(HintletReport.TITLE,
        new HintletReportModel(sourceModel.getHintletEngineHost()));
  }
}