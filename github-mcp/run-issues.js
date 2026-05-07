import "dotenv/config";
import axios from "axios";
import { issuesData } from "./issues-data.js";

const gh = axios.create({
  baseURL: "https://api.github.com",
  headers: {
    Authorization: `Bearer ${process.env.GITHUB_TOKEN}`,
    Accept: "application/vnd.github+json",
    "X-GitHub-Api-Version": "2022-11-28",
  },
});

const OWNER = process.env.GITHUB_OWNER;
const REPO  = process.env.GITHUB_REPO;
const delay = (ms) => new Promise(r => setTimeout(r, ms));

// ── 제목에서 계층 키 추출 ──────────────────────────────────────────────────
// [EPIC-1]       → { type: "epic",    epicId: "1" }
// [FEATURE-1-2]  → { type: "feature", epicId: "1", featureId: "2" }
// [TASK-1-2-3]   → { type: "task",    epicId: "1", featureId: "2", taskId: "3" }
function parseKey(title) {
  const epic    = title.match(/\[EPIC-(\d+)\]/);
  const feature = title.match(/\[FEATURE-(\d+)-(\d+)\]/);
  const task    = title.match(/\[TASK-(\d+)-(\d+)-(\d+)\]/);
  if (task)    return { type: "task",    epicId: task[1],    featureId: task[2],    taskId: task[3] };
  if (feature) return { type: "feature", epicId: feature[1], featureId: feature[2] };
  if (epic)    return { type: "epic",    epicId: epic[1] };
  return { type: "other" };
}

// ── GraphQL 공통 호출 함수 ───────────────────────────────────────────────
async function gql(query, variables = {}) {
  const res = await axios.post(
    "https://api.github.com/graphql",
    { query, variables },
    { headers: { Authorization: `Bearer ${process.env.GITHUB_TOKEN}` } }
  );
  if (res.data.errors) throw new Error(res.data.errors[0].message);
  return res.data.data;
}

// ── GraphQL: 이슈 삭제 ───────────────────────────────────────────────────
async function deleteIssue(nodeId) {
  await gql(`mutation($id:ID!){ deleteIssue(input:{issueId:$id}){ repository{id} } }`, { id: nodeId });
}

// ── GraphQL: 서브 이슈 연결 ──────────────────────────────────────────────
async function addSubIssue(parentNodeId, childNodeId) {
  await gql(
    `mutation($parent:ID!, $child:ID!){
       addSubIssue(input:{ issueId:$parent, subIssueId:$child }){
         issue{ number } subIssue{ number }
       }
     }`,
    { parent: parentNodeId, child: childNodeId }
  );
}

// ── STEP 1: 기존 이슈 전부 삭제 ──────────────────────────────────────────
console.log(`🗑️  기존 이슈 삭제 중... (${OWNER}/${REPO})\n`);
let page = 1;
while (true) {
  const { data: existing } = await gh.get(`/repos/${OWNER}/${REPO}/issues`, {
    params: { state: "all", per_page: 100, page },
  });
  // pull_request 항목 제외 (issues API가 PR도 반환함)
  const issues = existing.filter(i => !i.pull_request);
  if (issues.length === 0) break;
  for (const issue of issues) {
    try {
      await deleteIssue(issue.node_id);
      console.log(`🗑️  #${issue.number} 삭제됨: ${issue.title}`);
    } catch (e) {
      console.error(`❌ 삭제 실패 #${issue.number}: ${e.message}`);
    }
    await delay(300);
  }
  page++;
}
console.log("✅ 기존 이슈 삭제 완료\n");

// ── STEP 2: 라벨 생성 ────────────────────────────────────────────────────
console.log("── 라벨 생성 ──────────────────────────");
for (const label of issuesData.labels) {
  try {
    await gh.post(`/repos/${OWNER}/${REPO}/labels`, label);
    console.log(`🏷️  생성: ${label.name}`);
  } catch (e) {
    if (e.response?.status === 422) console.log(`⏭️  이미 존재: ${label.name}`);
    else console.error(`❌ 라벨 실패: ${label.name} - ${e.message}`);
  }
}

// ── STEP 3: 이슈 생성 (순서대로) + 번호/nodeId 맵 저장 ──────────────────
// 맵: "EPIC-1" → { number, nodeId }, "FEATURE-1-2" → { number, nodeId }
const issueMap = {};

console.log("\n── 이슈 생성 ──────────────────────────");
for (const issue of issuesData.issues) {
  try {
    const { data } = await gh.post(`/repos/${OWNER}/${REPO}/issues`, {
      title: issue.title,
      body: issue.body,
      labels: issue.labels,
    });
    console.log(`✅ #${data.number} ${issue.title}`);

    const k = parseKey(issue.title);
    const entry = { number: data.number, nodeId: data.node_id };
    if (k.type === "epic")    issueMap[`EPIC-${k.epicId}`]                            = entry;
    if (k.type === "feature") issueMap[`FEATURE-${k.epicId}-${k.featureId}`]          = entry;
    if (k.type === "task")    issueMap[`TASK-${k.epicId}-${k.featureId}-${k.taskId}`] = entry;

    await delay(600);
  } catch (e) {
    console.error(`❌ 실패: ${issue.title} - ${e.response?.data?.message || e.message}`);
  }
}

// ── STEP 4: 서브 이슈 연결 (GraphQL addSubIssue) ─────────────────────────
// FEATURE → EPIC의 서브이슈
// TASK    → FEATURE의 서브이슈
console.log("\n── 서브 이슈 연결 ─────────────────────");
for (const [key, entry] of Object.entries(issueMap)) {
  let parentEntry = null;

  const featureMatch = key.match(/^FEATURE-(\d+)-(\d+)$/);
  const taskMatch    = key.match(/^TASK-(\d+)-(\d+)-(\d+)$/);

  if (featureMatch) {
    parentEntry = issueMap[`EPIC-${featureMatch[1]}`];
  } else if (taskMatch) {
    parentEntry = issueMap[`FEATURE-${taskMatch[1]}-${taskMatch[2]}`];
  }

  if (!parentEntry) continue;

  try {
    await addSubIssue(parentEntry.nodeId, entry.nodeId);
    console.log(`🔗 #${parentEntry.number} ← #${entry.number} (${key})`);
    await delay(400);
  } catch (e) {
    console.error(`❌ 서브이슈 연결 실패 [${key}]: ${e.message}`);
  }
}

console.log("\n🎉 모든 작업 완료!");

