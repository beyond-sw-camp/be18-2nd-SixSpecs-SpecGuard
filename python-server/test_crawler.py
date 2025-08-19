import asyncio
from app.crawlers.velog_crawler import render_list_with_playwright, render_post_with_playwright

async def main():
    handle = "sangwon5579"
    links = await render_list_with_playwright(handle)
    print("총 글 개수:", len(links))
    print("샘플 링크 3개:", links[:3])

    if links:
        title, text, langs, tags, published = await render_post_with_playwright(links[0])
        print("제목:", title)
        print("코드 언어:", langs)
        print("태그:", tags)
        print("작성일:", published)
        print("본문 일부:", text[:200], "...")

if __name__ == "__main__":
    asyncio.run(main())
