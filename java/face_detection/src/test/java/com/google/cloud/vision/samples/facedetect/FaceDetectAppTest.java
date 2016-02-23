/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.vision.samples.facedetect;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.Vertex;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/** Unit tests for {@link FaceDetectApp}. */
@RunWith(JUnit4.class)
public class FaceDetectAppTest {
  private static final int MAX_RESULTS = 3;

  @Test public void detectFaces_withFace_returnsAtLeastOneFace() throws Exception {
    // Arrange
    FaceDetectApp appUnderTest = new FaceDetectApp(FaceDetectApp.getVisionService());

    // Act
    List<FaceAnnotation> faces =
        appUnderTest.detectFaces(Paths.get("../../data/face_detection/face.jpg"), MAX_RESULTS);

    // Assert
    assertThat(faces).named("face.jpg faces").isNotEmpty();
    assertThat(faces.get(0).getFdBoundingPoly().getVertices())
        .named("face.jpg face #0 FdBoundingPoly Vertices")
        .isNotEmpty();
  }

  @Test public void detectFaces_badImage_throwsException() throws Exception {
    FaceDetectApp appUnderTest = new FaceDetectApp(FaceDetectApp.getVisionService());

    try {
      appUnderTest.detectFaces(Paths.get("../../data/bad.txt"), MAX_RESULTS);
      fail("Expected IOException");
    } catch (IOException expected) {
      assertThat(expected.getMessage().toLowerCase())
          .named("IOException message")
          .contains("malformed request");
    }
  }

  @Test public void annotateWithFaces_manyFaces_outlinesFaces() throws Exception {
    // Arrange
    ImmutableList<FaceAnnotation> faces =
        ImmutableList.of(
            new FaceAnnotation()
                .setFdBoundingPoly(
                    new BoundingPoly().setVertices(ImmutableList.of(
                        new Vertex().setX(10).setY(5),
                        new Vertex().setX(20).setY(5),
                        new Vertex().setX(20).setY(25),
                        new Vertex().setX(10).setY(25)))),
            new FaceAnnotation()
                .setFdBoundingPoly(
                    new BoundingPoly().setVertices(ImmutableList.of(
                        new Vertex().setX(60).setY(50),
                        new Vertex().setX(70).setY(60),
                        new Vertex().setX(50).setY(60)))));
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

    // Act
    FaceDetectApp.annotateWithFaces(img, faces);

    // Assert
    assertThat(img.getRGB(10, 5) & 0x00ff00)
        .named("img face #1 vertex (10, 5) green channel")
        .isEqualTo(0x00ff00);
    assertThat(img.getRGB(20, 5) & 0x00ff00)
        .named("img face #1 vertex (20, 5) green channel")
        .isEqualTo(0x00ff00);
    assertThat(img.getRGB(20, 25) & 0x00ff00)
        .named("img face #1 vertex (20, 25) green channel")
        .isEqualTo(0x00ff00);
    assertThat(img.getRGB(10, 25) & 0x00ff00)
        .named("img face #1 vertex (10, 25) green channel")
        .isEqualTo(0x00ff00);
    assertThat(img.getRGB(60, 50) & 0x00ff00)
        .named("img face #2 vertex (60, 50) green channel")
        .isEqualTo(0x00ff00);
    assertThat(img.getRGB(70, 60) & 0x00ff00)
        .named("img face #2 vertex (70, 60) green channel")
        .isEqualTo(0x00ff00);
    assertThat(img.getRGB(50, 60) & 0x00ff00)
        .named("img face #2 vertex (50, 60) green channel")
        .isEqualTo(0x00ff00);
  }
}
