package com.example.csks_creatives.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveDuration
import com.example.csks_creatives.presentation.components.ui.ModernDateView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun EmployeeProfileComponent(
    employeeName: String,
    employeeJoinedTime: String,
    employeePassword: String,
    totalNumberOfTasksCompleted: String,
    isCompletedCountLoading: Boolean,
    approvedLeaves: List<LeaveRequest>,
    unApprovedLeaves: List<LeaveRequest>,
    rejectedLeaves: List<LeaveRequest>,
    onApproveLeave: ((LeaveRequest) -> Unit)? = null,
    onRejectLeave: ((LeaveRequest) -> Unit)? = null,
    onFetchCompletedCount: () -> Unit,
    coroutineScope: CoroutineScope
) {
    val pagerState = rememberPagerState(initialPage = 0)
    val tabTitles = listOf("Pending", "Approved", "Rejected", "Summary")

    val totalLeavesTaken = remember(approvedLeaves) {
        approvedLeaves.sumOf { if (it.leaveDuration == LeaveDuration.HALF_DAY) 0.5 else 1.0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = charCoalPurple),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(vividCerulean.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .border(1.dp, vividCerulean.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = employeeName.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = vividCerulean,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = employeeName, style = MaterialTheme.typography.titleLarge, color = white, fontWeight = FontWeight.Bold)
                        if (employeeJoinedTime.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Joined ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = silverGrey.copy(alpha = 0.5f)
                                )
                                ModernDateView(
                                    timeStamp = employeeJoinedTime,
                                    useRelativeTime = false,
                                    showTime = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = silverGrey.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DetailItem("Leaves Taken", totalLeavesTaken.toString(), vividCerulean)
                    DetailItem("Credentials", employeePassword, goldenRod)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tasks Completed Section
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onFetchCompletedCount() },
                    color = white.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, white.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(limeGreen.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = limeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Lifetime Completed Tasks",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = silverGrey
                                )
                                Text(
                                    text = totalNumberOfTasksCompleted.ifEmpty { "0" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = white,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (isCompletedCountLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = limeGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh count",
                                tint = silverGrey.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = vividCerulean,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, fontSize = 11.sp, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal) },
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> LeaveList(unApprovedLeaves, "No pending leave requests.", onApproveLeave, onRejectLeave)
                1 -> {
                    val now = Date()
                    val (futureLeaves, pastLeaves) = approvedLeaves.partition { it.leaveDate.toDate().after(now) }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (futureLeaves.isNotEmpty()) {
                            item { SectionTitle("Future Leaves") }
                            items(futureLeaves.size) { index -> LeaveRequestTaskItem(leaveRequest = futureLeaves[index]) }
                        }
                        if (pastLeaves.isNotEmpty()) {
                            item { SectionTitle("Past Leaves") }
                            items(pastLeaves.size) { index -> LeaveRequestTaskItem(leaveRequest = pastLeaves[index]) }
                        }
                        if (futureLeaves.isEmpty() && pastLeaves.isEmpty()) {
                            item { Text("No approved leave requests.", color = silverGrey, modifier = Modifier.padding(16.dp)) }
                        }
                    }
                }
                2 -> LeaveList(rejectedLeaves, "No rejected leave requests.", onApproveLeave)
                3 -> LeaveSummary(approvedLeaves)
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = silverGrey)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
        color = white,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun LeaveList(
    leaves: List<LeaveRequest>,
    emptyMessage: String,
    onApprove: ((LeaveRequest) -> Unit)? = null,
    onReject: ((LeaveRequest) -> Unit)? = null
) {
    if (leaves.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage, color = silverGrey, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(leaves.size) { index ->
                LeaveRequestTaskItem(
                    leaveRequest = leaves[index],
                    onApproval = onApprove?.let { { it(leaves[index]) } },
                    onReject = onReject?.let { { it(leaves[index]) } }
                )
            }
        }
    }
}

@Composable
private fun LeaveSummary(approvedLeaves: List<LeaveRequest>) {
    if (approvedLeaves.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No approved leaves to summarize.", color = silverGrey)
        }
    } else {
        val groupedByYearMonth = approvedLeaves
            .sortedByDescending { it.leaveDate.toDate() }
            .groupBy {
                val date = it.leaveDate.toDate()
                val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
                "$month $year"
            }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groupedByYearMonth.forEach { (monthYear, leaves) ->
                item {
                    val leavesInMonth = leaves.sumOf { if (it.leaveDuration == LeaveDuration.HALF_DAY) 0.5 else 1.0 }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = charCoalPurple.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = monthYear, color = white, fontWeight = FontWeight.Medium)
                            Text(text = "$leavesInMonth Days", color = vividCerulean, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveRequestTaskItem(
    leaveRequest: LeaveRequest,
    onApproval: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val statusColor = when (leaveRequest.approvedStatus) {
        LeaveApprovalStatus.APPROVED -> limeGreen
        LeaveApprovalStatus.UN_APPROVED -> vividCerulean
        LeaveApprovalStatus.REJECTED -> Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    ModernDateView(leaveRequest.leaveDate.toDate().time.toString(), useRelativeTime = false)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = leaveRequest.leaveDuration.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = silverGrey.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = leaveRequest.approvedStatus.name,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = leaveRequest.leaveReason, style = MaterialTheme.typography.bodyMedium, color = silverGrey)

            if (onApproval != null || onReject != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (onReject != null) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Reject")
                        }
                    }
                    if (onApproval != null) {
                        Button(
                            onClick = onApproval,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = limeGreen)
                        ) {
                            Text("Approve", color = white)
                        }
                    }
                }
            }
        }
    }
}
