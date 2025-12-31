package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import com.jiagu.jgcompose.paging.LazyColumnPaging
import com.jiagu.jgcompose.paging.LazyGridPaging
import com.jiagu.jgcompose.theme.ComposeTheme
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllPagingPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val items = flowOf(PagingData.from(listOf("Item 1", "Item 2", "Item 3")))
            LazyColumnPaging(items = items, item = { item ->
                Text(text = item)
            }, onRefresh = {})
            Spacer(modifier = Modifier.height(80.dp))
            LazyGridPaging(
                columns = GridCells.Fixed(2),
                items = items,
                item = { item ->
                    Text(text = item)
                },
                onRefresh = {}
            )
        }
    }
}
