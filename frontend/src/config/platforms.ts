/** 平台编程语言选项 */
export interface PlatformLanguage {
  label: string
  value: string
  monacoLang: string
}

/** 各平台支持的编程语言 */
export const PLATFORM_LANGUAGES: Record<string, PlatformLanguage[]> = {
  leetcode: [
    { label: 'Java', value: 'java', monacoLang: 'java' },
    { label: 'Python3', value: 'python3', monacoLang: 'python' },
    { label: 'C++', value: 'cpp', monacoLang: 'cpp' },
  ],
  luogu: [
    { label: 'C++17', value: 'cpp', monacoLang: 'cpp' },
    { label: 'Java17', value: 'java', monacoLang: 'java' },
    { label: 'Python3', value: 'python3', monacoLang: 'python' },
    { label: 'C', value: 'c', monacoLang: 'c' },
  ],
}

/** 洛谷默认代码模板（洛谷不提供模板，给基础骨架） */
export const DEFAULT_CODE_TEMPLATES: Record<string, Record<string, string>> = {
  luogu: {
    cpp: '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    \n    return 0;\n}\n',
    java: 'import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        \n    }\n}\n',
    python3: 'import sys\ninput = sys.stdin.readline\n\n',
    c: '#include <stdio.h>\n\nint main() {\n    \n    return 0;\n}\n',
  },
}

/** 平台显示名 */
export const PLATFORM_LABELS: Record<string, string> = {
  leetcode: 'LeetCode',
  luogu: '洛谷',
}
