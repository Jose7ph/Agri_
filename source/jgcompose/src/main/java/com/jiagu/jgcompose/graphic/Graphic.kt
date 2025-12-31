package com.jiagu.jgcompose.graphic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.filament.IndexBuffer
import com.google.android.filament.RenderableManager.PrimitiveType
import com.google.android.filament.VertexBuffer
import com.jiagu.jgcompose.theme.ComposeTheme
import io.github.sceneview.Scene
import io.github.sceneview.geometries.Geometry
import io.github.sceneview.geometries.setIndices
import io.github.sceneview.geometries.setVertices
import io.github.sceneview.math.Position
import io.github.sceneview.math.Transform
import io.github.sceneview.node.MeshNode
import io.github.sceneview.node.SphereNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNode
import io.github.sceneview.rememberNodes


@Composable
fun GraphicTrack(
    modifier: Modifier = Modifier,
    points: List<Position>,
) {
    val transformedPoints = transformedPoints(points)
    //x,y往左下角平移方便显示在正中央
    val tf = Transform(position = Position(x = -0.5f, y = -0.5f))
    Box(modifier = modifier) {
        val engine = rememberEngine()
        val centerNode = rememberNode(engine)
        val cameraNode = rememberCameraNode(engine) {
            position = Position(z = 2f)
            lookAt(centerNode)
        }
        val materialLoader = rememberMaterialLoader(engine = engine)
        val childNodes = rememberNodes()
        //航点
        val pointNodes = rememberNode(engine) {
            transformedPoints.forEach {
                addChildNode(
                    SphereNode(
                        engine = engine,
                        radius = 0.03f,
                        materialInstance = materialLoader.createColorInstance(
                            Color.Gray
                        )
                    ).apply {
                        position = it
                    }
                )
            }
        }.apply {
            transform = tf
        }
        childNodes.add(pointNodes)
        //航线
        childNodes.add(
            MeshNode(
                engine = engine,
                primitiveType = PrimitiveType.LINE_STRIP,
                vertexBuffer = VertexBuffer.Builder()
                    .bufferCount(1)
                    .vertexCount(transformedPoints.size)
                    .attribute(
                        VertexBuffer.VertexAttribute.POSITION,
                        0,
                        VertexBuffer.AttributeType.FLOAT3,
                        0, 12
                    )
                    .build(engine).apply {
                        setVertices(
                            engine, vertices =
                            transformedPoints.map {
                                Geometry.Vertex(it)
                            }
                        )
                    },
                indexBuffer = IndexBuffer.Builder().indexCount(transformedPoints.size)
                    .build(engine)
                    .apply {
                        setIndices(
                            engine = engine,
                            indices = List(transformedPoints.size) { index -> index }.toList()
                        )
                    },
                materialInstance = materialLoader.createColorInstance(Color.White),
                boundingBox = com.google.android.filament.Box(
                    0f,
                    0f,
                    0f,
                    9000f,
                    9000f,
                    9000f
                )
            ).apply {
                transform = tf
            }
        )
        //坐标 可不显示
        childNodes.add(
            MeshNode(
                engine = engine,
                primitiveType = PrimitiveType.LINES,
                vertexBuffer = VertexBuffer.Builder()
                    .bufferCount(1)
                    .vertexCount(6)
                    .attribute(
                        VertexBuffer.VertexAttribute.POSITION,
                        0,
                        VertexBuffer.AttributeType.FLOAT3,
                        0, 12
                    )
                    .build(engine).apply {
                        setVertices(
                            engine, vertices = listOf(
                                //x
                                Geometry.Vertex(
                                    Position(
                                        x = -0.5f,
                                        y = 0f,
                                        z = 0f
                                    )
                                ),
                                Geometry.Vertex(
                                    Position(
                                        x = 0.5f,
                                        y = 0f,
                                        z = 0f
                                    )
                                ),
                                //y
                                Geometry.Vertex(
                                    Position(
                                        x = 0f,
                                        y = -0.5f,
                                        z = 0f
                                    )
                                ),
                                Geometry.Vertex(
                                    Position(
                                        x = 0f,
                                        y = 0.5f,
                                        z = 0f
                                    )
                                ),
                                //z
                                Geometry.Vertex(
                                    Position(
                                        x = 0f,
                                        y = 0f,
                                        z = -0.5f
                                    )
                                ),
                                Geometry.Vertex(
                                    Position(
                                        x = 0f,
                                        y = 0f,
                                        z = 0.5f
                                    )
                                ),
                            )
                        )
                    },
                indexBuffer = IndexBuffer.Builder().indexCount(6)
                    .build(engine)
                    .apply {
                        setIndices(engine = engine, indices = listOf(0, 1, 2, 3, 4, 5))
                    },
                materialInstance = materialLoader.createColorInstance(Color.Green),
                boundingBox = com.google.android.filament.Box(
                    0f,
                    0f,
                    0f,
                    9000f,
                    9000f,
                    9000f
                )
            )
        )
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            childNodes = childNodes,
            cameraNode = cameraNode,
        )
    }
}

/**
 * 数据转换
 *
 * @param points
 * @return
 */
private fun transformedPoints(points: List<Position>): List<Position> {
    // 计算数据的最小值和最大值
    val xMin = points.minOf { it.x }
    val xMax = points.maxOf { it.x }
    val yMin = points.minOf { it.y }
    val yMax = points.maxOf { it.y }
    val zMin = points.minOf { it.z }
    val zMax = points.maxOf { it.z }
    val scaleX = 1f / (xMax - xMin)
    val scaleY = 1f / (yMax - yMin)
    val scaleZ = 1f / (zMax - zMin)
    //取x、y最小比例
    val scale = minOf(scaleX, scaleY, scaleZ)
    return points.map {
        Position(
            x = ((it.x - xMin) * scale),
            y = ((it.y - yMin) * scale),
            z = ((it.z - zMin) * scale)
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun GraphicPreview() {
    ComposeTheme {
        Column {
            GraphicTrack(
                modifier = Modifier, points = listOf(
                    Position(0f, 0f, 0f),
                    Position(20f, 0f, 0f),
                    Position(20f, 10f, 0f),
                    Position(0f, 10f, 0f),
                    Position(0f, 20f, 0f),
                    Position(20f, 20f, 0f),
                )
            )
        }
    }
}