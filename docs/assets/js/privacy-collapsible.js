/**
 * Privacy Policy - Collapsible Sections for Mobile
 * Enhances mobile UX by allowing sections to be collapsed/expanded
 */

(function() {
  'use strict';
  
  // Only enable collapsible behavior on mobile devices
  const isMobile = () => window.innerWidth < 768;
  
  // Initialize collapsible sections
  function initCollapsibleSections() {
    if (!isMobile()) {
      // Remove collapsed class on desktop
      document.querySelectorAll('.privacy-policy section.collapsed').forEach(section => {
        section.classList.remove('collapsed');
      });
      return;
    }
    
    const sections = document.querySelectorAll('.privacy-policy section');
    
    sections.forEach((section, index) => {
      // Keep first section (introduction) expanded by default
      if (index > 0) {
        section.classList.add('collapsed');
      }
      
      // Add click handler to section heading
      const heading = section.querySelector('h3');
      if (heading) {
        heading.addEventListener('click', (e) => {
          e.stopPropagation();
          toggleSection(section);
        });
        
        // Make heading keyboard accessible
        heading.setAttribute('tabindex', '0');
        heading.setAttribute('role', 'button');
        heading.setAttribute('aria-expanded', !section.classList.contains('collapsed'));
        
        // Handle keyboard interaction
        heading.addEventListener('keydown', (e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            toggleSection(section);
          }
        });
      }
    });
  }
  
  // Toggle section collapsed state
  function toggleSection(section) {
    const isCollapsed = section.classList.toggle('collapsed');
    const heading = section.querySelector('h3');
    
    if (heading) {
      heading.setAttribute('aria-expanded', !isCollapsed);
    }
    
    // Smooth scroll to section if expanding
    if (!isCollapsed) {
      setTimeout(() => {
        section.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }, 100);
    }
  }
  
  // Handle window resize
  let resizeTimer;
  function handleResize() {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(() => {
      initCollapsibleSections();
    }, 250);
  }
  
  // Initialize on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCollapsibleSections);
  } else {
    initCollapsibleSections();
  }
  
  // Re-initialize on window resize
  window.addEventListener('resize', handleResize);
  
  // Handle hash navigation (anchor links)
  function handleHashNavigation() {
    const hash = window.location.hash;
    if (hash) {
      const targetSection = document.querySelector(hash);
      if (targetSection && targetSection.classList.contains('collapsed')) {
        targetSection.classList.remove('collapsed');
        const heading = targetSection.querySelector('h3');
        if (heading) {
          heading.setAttribute('aria-expanded', 'true');
        }
      }
    }
  }
  
  // Handle hash changes
  window.addEventListener('hashchange', handleHashNavigation);
  
  // Check hash on load
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', handleHashNavigation);
  } else {
    handleHashNavigation();
  }
  
})();
