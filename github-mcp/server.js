import "dotenv/config";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import axios from "axios";

// ── GitHub REST API 클라이언트 ──────────────────────────────────────────────
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

// ── MCP 서버 인스턴스 ───────────────────────────────────────────────────────
const server = new McpServer({
  name: "github-issue-manager",
  version: "1.0.0",
});

// ────────────────────────────────────────────────────────────────────────────
// Tool 1: 라벨 생성
// ────────────────────────────────────────────────────────────────────────────
server.tool(
  "create_label",
  "GitHub 저장소에 라벨을 생성합니다",
  {
    name:        z.string().describe("라벨 이름"),
    color:       z.string().describe("라벨 색상 (hex, # 제외)"),
    description: z.string().optional().describe("라벨 설명"),
  },
  async ({ name, color, description }) => {
    try {
      const { data } = await gh.post(`/repos/${OWNER}/${REPO}/labels`, {
        name, color, description,
      });
      return { content: [{ type: "text", text: `✅ 라벨 생성: ${data.name} (#${data.color})` }] };
    } catch (e) {
      if (e.response?.status === 422) {
        return { content: [{ type: "text", text: `⏭️ 라벨 이미 존재: ${name}` }] };
      }
      throw new Error(`라벨 생성 실패: ${e.message}`);
    }
  }
);

// ────────────────────────────────────────────────────────────────────────────
// Tool 2: 이슈 생성
// ────────────────────────────────────────────────────────────────────────────
server.tool(
  "create_issue",
  "GitHub 저장소에 이슈를 생성합니다",
  {
    title:  z.string().describe("이슈 제목"),
    body:   z.string().describe("이슈 본문 (마크다운)"),
    labels: z.array(z.string()).optional().describe("라벨 목록"),
  },
  async ({ title, body, labels }) => {
    const { data } = await gh.post(`/repos/${OWNER}/${REPO}/issues`, {
      title, body, labels,
    });
    return {
      content: [{
        type: "text",
        text: `✅ 이슈 생성: #${data.number} ${data.title}\n🔗 ${data.html_url}`,
      }],
    };
  }
);

// ────────────────────────────────────────────────────────────────────────────
// Tool 3: 이슈 목록 조회
// ────────────────────────────────────────────────────────────────────────────
server.tool(
  "list_issues",
  "GitHub 저장소의 이슈 목록을 조회합니다",
  {
    state:  z.enum(["open", "closed", "all"]).default("open"),
    labels: z.string().optional().describe("쉼표로 구분된 라벨 필터"),
    per_page: z.number().default(20),
  },
  async ({ state, labels, per_page }) => {
    const { data } = await gh.get(`/repos/${OWNER}/${REPO}/issues`, {
      params: { state, labels, per_page },
    });
    const text = data.map(i => `#${i.number} [${i.labels.map(l=>l.name).join(",")}] ${i.title}`).join("\n");
    return { content: [{ type: "text", text: text || "이슈 없음" }] };
  }
);

// ────────────────────────────────────────────────────────────────────────────
// Tool 4: 프로젝트 전체 이슈 일괄 생성 (Epic-Feature-Task 구조)
// ────────────────────────────────────────────────────────────────────────────
server.tool(
  "create_project_issues",
  "prephub 프로젝트의 Epic-Feature-Task 이슈 전체를 일괄 생성합니다",
  {},
  async () => {
    const { issuesData } = await import("./issues-data.js");
    const results = [];

    // 1) 라벨 먼저 생성
    for (const label of issuesData.labels) {
      try {
        await gh.post(`/repos/${OWNER}/${REPO}/labels`, label);
        results.push(`🏷️  라벨: ${label.name}`);
      } catch (e) {
        if (e.response?.status === 422) results.push(`⏭️  라벨 존재: ${label.name}`);
      }
    }

    // 2) 이슈 순서대로 생성 (Epic → Feature → Task 순)
    for (const issue of issuesData.issues) {
      try {
        const { data } = await gh.post(`/repos/${OWNER}/${REPO}/issues`, {
          title: issue.title,
          body: issue.body,
          labels: issue.labels,
        });
        results.push(`✅ #${data.number} ${issue.title}`);
        await new Promise(r => setTimeout(r, 600)); // rate limit 방지
      } catch (e) {
        results.push(`❌ 실패: ${issue.title} - ${e.message}`);
      }
    }

    return { content: [{ type: "text", text: results.join("\n") }] };
  }
);

// ── 서버 시작 ───────────────────────────────────────────────────────────────
const transport = new StdioServerTransport();
await server.connect(transport);
console.error("✅ GitHub MCP 서버 시작됨");

